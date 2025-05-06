package com.redstar.redefinencm.activity.MainActivity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.SettingPage
import com.redstar.redefinencm.data.db.DatabaseProvider
import com.redstar.redefinencm.data.repository.Repository
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val viewModel = MainViewModel(Repository(DatabaseProvider.getDao(applicationContext)))

        setContent {
            RedefineNCMTheme {
                val context = LocalContext.current
                val navController = rememberNavController()

                val items = listOf(
                    NavigationItem("Home", Icons.Filled.Home, "home"),
                    NavigationItem("My", Icons.Filled.Person, "my"),
                    NavigationItem("Settings", Icons.Filled.Settings, "settings")
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Use a Surface with the background color to ensure proper drawing under system bars
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        // Don't use default system window insets - we'll handle them ourselves
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            // Apply padding from the scaffold but not for system bars
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
