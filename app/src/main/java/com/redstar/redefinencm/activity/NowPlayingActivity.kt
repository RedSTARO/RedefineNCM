package com.redstar.redefinencm.activity

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.services.playbackService
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.guava.await

class NowPlayingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(title = { Text("Now Playing") })
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        PlaybackController()
                    }
                }
            }
        }
    }
}

@Composable
fun PlaybackController() {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val context = RedefineNCMApplication.getApplicationContext() as Context

    LaunchedEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, playbackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaController = controllerFuture.await()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: NowPlaying
            Text(text = "当前播放", fontSize = 20.sp, style = MaterialTheme.typography.headlineSmall)
            Text(text = mediaController?.mediaMetadata?.title.toString())
            Text(text = mediaController?.mediaMetadata?.artist.toString())
            AsyncImage(model = mediaController?.mediaMetadata?.artworkUri, contentDescription = null)

            Spacer(modifier = Modifier.height(20.dp))

            // 播放/暂停按钮
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { mediaController?.pause() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "暂停")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { mediaController?.play() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "播放")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { mediaController?.seekToNext() },
                    modifier = Modifier.weight(1f)
                ){
                    Text(text = "下一首")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { mediaController?.seekToPrevious() },
                    modifier = Modifier.weight(1f)
                ){
                    Text(text = "上一首")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ){
                    Text(text = "添加到喜欢")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {mediaController?.setShuffleModeEnabled(!mediaController!!.shuffleModeEnabled)},
                    modifier = Modifier.weight(1f)
                ){
                    Text(text = "随机")
                }
            }
        }
    }
}
