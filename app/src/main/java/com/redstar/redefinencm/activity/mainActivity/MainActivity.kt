 package com.redstar.redefinencm.activity.mainActivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.SettingPage
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.SettingProvider
import com.redstar.redefinencm.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel = MainViewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lateinit var importSettingLauncher: ActivityResultLauncher<Intent>
        // 注册 Launcher
        importSettingLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        SettingProvider.importAppSettingFromUri(
                            RedefineNCMApplication.getApplicationContext(), uri
                        )
                    }
                }
            }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val widthClass = windowSizeClass.widthSizeClass
            RedefineNCMTheme {
                val context = LocalContext.current
                val navController = rememberNavController()

                val items = listOf(
                    NavigationItem("Recommend", Icons.Filled.Home, "recommend"),
                    NavigationItem("My", Icons.Filled.Person, "my"),
                    NavigationItem("Settings", Icons.Filled.Settings, "settings"),
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Use a Surface with the background color to ensure proper drawing under system bars
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        // Don't use default system window insets - we'll handle them ourselves
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        floatingActionButton = { MiniNowPlaying(context, viewModel = viewModel) },
                        bottomBar = {
                            if (widthClass == WindowWidthSizeClass.Compact) {
                                ResponsiveNavigation(navController, items, currentRoute, widthClass)
                            }
                        },
                    ) { innerPadding ->
                        Row() {
                            if (widthClass != WindowWidthSizeClass.Compact) {
                                ResponsiveNavigation(navController, items, currentRoute, widthClass)
                            }
                            SharedTransitionLayout {
                                val sharedTransitionScope = this
                                NavHost(
                                    navController = navController,
                                    startDestination = "recommend",
                                    // Apply padding from the scaffold but not for system bars
                                    modifier = Modifier.padding(innerPadding),
                                ) {
                                    composable("recommend") {
                                        AnimatedVisibility(visible = true) {
                                            RecommendPage(
                                                navController,
                                                viewModel,
                                                sharedTransitionScope,
                                                this,
                                            )
                                        }
                                    }
                                    composable("search") {
                                        AnimatedVisibility(visible = true) {
                                            SearchDemoPage(
                                                navController,
                                                sharedTransitionScope,
                                                this,
                                            )
                                        }
                                    }

                                    composable("recommend/playlistDetailPage/{songId}") { backStackEntry ->
                                        val songId = backStackEntry.arguments?.getString("songId")
                                        if (BuildConfig.DEBUG) {
                                            Log.d("Main", "SongList ID: $songId")
                                        }
                                        ShowPlaylistDetailPage(
                                            viewModel = viewModel,
                                            songlistID = songId!!.toLong(),
                                        )
                                    }

                                    composable("my") {
                                        ShowUserPlaylistPage(
                                            navController = navController,
                                            viewModel = viewModel,
                                        )
                                    }
                                    composable("my/playlistDetailPage/{songId}") { backStackEntry ->
                                        val songId = backStackEntry.arguments?.getString("songId")
                                        if (BuildConfig.DEBUG) {
                                            Log.d("Main", "SongList ID: $songId")
                                        }
                                        ShowPlaylistDetailPage(
                                            viewModel = viewModel,
                                            songlistID = songId!!.toLong(),
                                        )
                                    }
                                    composable("settings") {
                                        SettingPage(this@MainActivity, importSettingLauncher)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        viewModel.savePlayerStatus()
        super.onPause()
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun ResponsiveNavigation(
    navController: NavHostController,
    items: List<NavigationItem>,
    currentRoute: String?,
    widthClass: WindowWidthSizeClass,
) {
    if (widthClass == WindowWidthSizeClass.Compact) {
        // 底部导航栏
        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = currentRoute?.startsWith(item.route) == true,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo("my") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    } else {
        // 侧边导航栏
        NavigationRail {
            items.forEach { item ->
                NavigationRailItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = currentRoute?.startsWith(item.route) == true,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo("my") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}
