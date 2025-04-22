package com.redstar.redefinencm.activity.MainActivity

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.MoreExecutors
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.SettingPage
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.services.playbackService
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
                var uid by remember { mutableStateOf<Long?>(null) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    NavigationItem("Home", Icons.Filled.Home, "home"),
                    NavigationItem("My", Icons.Filled.Person, "my"),
                    NavigationItem("Settings", Icons.Filled.Settings, "settings")
                )

                LaunchedEffect(Unit) {
                    uid = retrofit.userAccount().account.id
                    if (BuildConfig.DEBUG) {
                        Log.d("Main", "UID: $uid")
                    }
                }
                Scaffold(
                    floatingActionButton = { MiniNowPlaying(context) },
                    bottomBar = {
                        NavigationBar {
                            items.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    selected = currentRoute?.startsWith(item.route) == true,
                                    onClick = {
                                        Log.d("NavDebug", "currentRoute: $currentRoute, item.route: ${item.route}")
                                        navController.navigate(item.route) {
                                            // To prevent back navigation
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    if (uid != null) {
                        // 使用 NavHost 管理导航
                        NavHost(
                            navController = navController,  // 传入 NavController 实例
                            startDestination = "my",
                            Modifier.padding(innerPadding)// 指定开始的页面
                        ) {
                            composable("my") {
                                ShowUserPlaylistPage(
                                    retrofit = retrofit,
                                    uid = uid!!,
                                    navController = navController
                                )
                            }
                            composable("home") {
                                PlaceHolderScreen()
                            }

                            composable("my/playlistDetailPage/{songId}") { backStackEntry ->
                                val songId = backStackEntry.arguments?.getString("songId")
                                if (BuildConfig.DEBUG) {
                                    Log.d("Main", "SongList ID: $songId")
                                }
                                ShowPlaylistDetailPage(
                                    retrofit = retrofit,
                                    songlistID = songId!!.toLong()
                                )
                            }

                            composable("settings") {
                                SettingPage()
                            }
                        }
                        Spacer(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (BuildConfig.DEBUG) {
            Log.d("MainActivity", "onStart")
        }
        val sessionToken = SessionToken(this, ComponentName(this, playbackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            // MediaController is available here with controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        val sessionToken = SessionToken(this, ComponentName(this, playbackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        MediaController.releaseFuture(controllerFuture)
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)