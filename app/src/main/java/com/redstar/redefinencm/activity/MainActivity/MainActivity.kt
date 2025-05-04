package com.redstar.redefinencm.activity.MainActivity

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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Database
import androidx.room.Room
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.SettingPage
import com.redstar.redefinencm.data.db.AppDatabase
import com.redstar.redefinencm.data.db.DatabaseProvider
import com.redstar.redefinencm.data.db.dao.UserDao
import com.redstar.redefinencm.data.repository.UserRepository
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RedefineNCMTheme {
                val context = LocalContext.current
                val navController = rememberNavController()

                val userDao = DatabaseProvider.getUserDao(applicationContext)
                val userRepository = UserRepository(userDao)
                val viewModel = MainViewModel(userRepository)


                val items = listOf(
                    NavigationItem("Home", Icons.Filled.Home, "home"),
                    NavigationItem("My", Icons.Filled.Person, "my"),
                    NavigationItem("Settings", Icons.Filled.Settings, "settings")
                )

                if (viewModel.uid != 0L) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        floatingActionButton = { MiniNowPlaying(context, viewModel = viewModel) },
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
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            }
                            composable("my/playlistDetailPage/{songId}") { backStackEntry ->
                                val songId = backStackEntry.arguments?.getString("songId")
                                if (BuildConfig.DEBUG) {
                                    Log.d("Main", "SongList ID: $songId")
                                }
                                ShowPlaylistDetailPage(
                                    viewModel = viewModel,
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
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
