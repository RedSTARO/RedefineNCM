package com.redstar.redefinencm.activity.MainActivity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.google.common.util.concurrent.MoreExecutors
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.NowPlayingActivity
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
                val context = RedefineNCMApplication.getApplicationContext() as Context
                val metadataFlow = remember { MutableStateFlow<MediaMetadata?>(null) }
                val metadata by metadataFlow.collectAsState()

                LaunchedEffect(Unit) {
                    val sessionToken =
                        SessionToken(context, ComponentName(context, playbackService::class.java))
                    val controllerFuture =
                        MediaController.Builder(context, sessionToken).buildAsync()
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
                        FloatingActionButton(
                            onClick = {
                                // 启动 NowPlayingActivity
                                val context = this
                                context.startActivity(Intent(context, NowPlayingActivity::class.java))
                            },
                            modifier = Modifier
                                .size(100.dp)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            AsyncImage(model = metadata?.artworkUri, contentDescription = null)
                        }
                    },
                ) { innerPadding ->
                    Surface {
                        MainScreen()
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


