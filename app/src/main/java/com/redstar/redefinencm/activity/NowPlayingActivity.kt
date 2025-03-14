package com.redstar.redefinencm.activity

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
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
    val context = LocalContext.current

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
