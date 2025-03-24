package com.redstar.redefinencm.activity.MainActivity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.NowPlayingActivity
import com.redstar.redefinencm.services.playbackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.guava.await

@Composable
fun MiniNowPlaying(context: Context) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(mediaController?.isPlaying == true) }
    val applicationContext = RedefineNCMApplication.getApplicationContext() as Context
    val metadataFlow = remember { MutableStateFlow<MediaMetadata?>(null) }
    val metadata by metadataFlow.collectAsState()

    LaunchedEffect(Unit) {
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
                    IconButton(onClick = {
                        if (isPlaying) mediaController?.pause() else mediaController?.play()
                        isPlaying = mediaController?.isPlaying == true
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Home else Icons.Default.PlayArrow,
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
}