package com.redstar.redefinencm.activity.MainActivity

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
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
                Scaffold(
                    floatingActionButton = {
                        SimpleExoPlayerScreen(context = this)
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
        Log.d("MainActivity", "onStart")
        val sessionToken = SessionToken(this, ComponentName(this, playbackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//        controllerFuture.addListener(
//            {
//                // Call controllerFuture.get() to retrieve the MediaController.
//                // MediaController implements the Player interface, so it can be
//                // attached to the PlayerView UI component.
//                playerView.setPlayer(controllerFuture.get())
//            },
//            MoreExecutors.directExecutor()
//        )
    }

    override fun onStop() {
        super.onStop()
        val sessionToken = SessionToken(this, ComponentName(this, playbackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        MediaController.releaseFuture(controllerFuture)
    }
}

@Composable
fun SimpleExoPlayerScreen(context: Context) {
    val player = remember { ExoPlayer.Builder(context).build() }

    LaunchedEffect(Unit) {
        val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
        val url = retrofit.songUrlV1(listOf(33894312), "jymaster").data[0].url
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { player.play() }) {
            Text("播放")
        }
        Button(onClick = { player.pause() }) {
            Text("暂停")
        }
        Button(onClick = { player.stop() }) {
            Text("停止")
        }
    }
}

