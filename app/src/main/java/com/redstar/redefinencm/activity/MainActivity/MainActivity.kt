package com.redstar.redefinencm.activity.MainActivity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.common.util.concurrent.MoreExecutors
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.NowPlayingActivity
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.services.playbackService
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.guava.await


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                var mediaController by remember { mutableStateOf<MediaController?>(null) }
                val applicationContext = RedefineNCMApplication.getApplicationContext() as Context
                LocalContext.current
                val context = LocalContext.current
                val metadataFlow = remember { MutableStateFlow<MediaMetadata?>(null) }
                val metadata by metadataFlow.collectAsState()
                val navController = rememberNavController()
                if (BuildConfig.DEBUG) {
                    Log.d("Main", "MainStartup, Nav Controller: $navController")
                }
                val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
                var uid by remember { mutableStateOf<Long?>(null) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    NavigationItem("Home", Icons.Filled.Home, "home"),
                    NavigationItem("My", Icons.Filled.Person, "my"),
//                    NavigationItem("Settings", Icons.Filled.Settings, "settings")
                )

                LaunchedEffect(Unit) {
                    uid = retrofit.userAccount().account.id
                    if (BuildConfig.DEBUG) {
                        Log.d("Main", "UID: $uid")
                    }
                    val sessionToken =
                        SessionToken(
                            applicationContext,
                            ComponentName(applicationContext, playbackService::class.java)
                        )
                    val controllerFuture =
                        MediaController.Builder(applicationContext, sessionToken).buildAsync()
                    mediaController = controllerFuture.await()
                    mediaController?.let { controller ->
                        val listener = object : Player.Listener {
                            override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                                metadataFlow.value = metadata
                            }
                        }
                        controller.addListener(listener)
                        metadataFlow.value = controller.mediaMetadata // 初始化
                    }
                }
                Scaffold(
                    floatingActionButton = {
                        Card(
                            modifier = Modifier
                                .size(width = 250.dp, height = 100.dp)
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(0.7f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = metadata?.title.toString(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // 播放控制按钮
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(onClick = { mediaController?.seekToPrevious() }) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowLeft,
                                                contentDescription = "Previous"
                                            )
                                        }
                                        IconButton(onClick = { if (mediaController?.isPlaying == true) mediaController?.pause() else mediaController?.play() }) {
                                            Icon(
                                                imageVector = if (mediaController?.isPlaying == true) Icons.Default.Home else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause"
                                            )
                                        }
                                        IconButton(onClick = { mediaController?.seekToNext() }) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Next"
                                            )
                                        }
                                    }

                                }
                                AsyncImage(
                                    model = metadata?.artworkUri,
                                    contentDescription = "Album art",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .fillMaxSize()
                                        .clickable(onClick = {
                                            // 启动 NowPlayingActivity
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    NowPlayingActivity::class.java
                                                )
                                            )
                                        })
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            items.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    selected = currentRoute == item.route,
                                    onClick = {
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
                            composable("playlistDetailPage/{songId}") { backStackEntry ->
                                val songId = backStackEntry.arguments?.getString("songId")
                                if (BuildConfig.DEBUG) {
                                    Log.d("Main", "SongList ID: $songId")
                                }
                                ShowPlaylistDetailPage(
                                    retrofit = retrofit,
                                    songlistID = songId!!.toLong()
                                )
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