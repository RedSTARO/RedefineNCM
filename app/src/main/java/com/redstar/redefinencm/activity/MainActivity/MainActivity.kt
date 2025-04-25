package com.redstar.redefinencm.activity.MainActivity

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.*
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

                val items = listOf(
                    NavigationItem("Home", Icons.Filled.Home, "home"),
                    NavigationItem("My", Icons.Filled.Person, "my"),
                    NavigationItem("Settings", Icons.Filled.Settings, "settings")
                )

                LaunchedEffect(Unit) {
                    try {
                        uid = retrofit.userAccount().account.id
                        if (BuildConfig.DEBUG) {
                            Log.d("Main", "UID: $uid")
                        }
                    } catch (e: Exception) {
                        Log.e("Main", "Failed to fetch UID: ${e.message}")
                    }
                }

                // 只有 uid 存在时再显示导航界面
                if (uid != null) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

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
                                            navController.navigate(item.route) {
                                                // 使用字符串而不是 graph，防止初始化异常
                                                popUpTo("my") {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        },
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "my",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                PlaceHolderScreen()
                            }
                            composable("my") {
                                ShowUserPlaylistPage(
                                    retrofit = retrofit,
                                    uid = uid!!,
                                    navController = navController
                                )
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
            // MediaController is available here
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
