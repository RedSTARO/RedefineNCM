package com.redstar.redefinencm.activity.mainActivity

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.NowPlayingActivity
import com.redstar.redefinencm.util.ImageParser
import com.redstar.redefinencm.viewmodel.MainViewModel
import com.skydoves.cloudy.cloudy

@Composable
fun MiniNowPlaying(context: Context, viewModel: MainViewModel) {
    val metadata by viewModel.nowPlayingMetadata.collectAsState()
    val isPlaying by viewModel.nowPayingIsPlaying.collectAsState()
    val initialColor = MaterialTheme.colorScheme
    var themeColor by remember { mutableStateOf(initialColor.primary) }
//    var textColor by remember { mutableStateOf(initialColor.onPrimary) }
    val mediaController by viewModel.mediaController.collectAsState()

    Card(
        modifier = Modifier
            .size(width = 250.dp, height = 100.dp)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColor,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                Text(
                    text = (metadata?.title ?: "Not Playing").toString(),
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    textAlign = TextAlign.Center,
                )
                // 播放控制按钮
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.weight(1f),
                ) {
                    IconButton(onClick = { mediaController?.seekToPrevious() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous",
                        )
                    }
                    IconButton(onClick = {
                        if (isPlaying) mediaController?.pause() else mediaController?.play()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                        )
                    }
                    IconButton(onClick = { mediaController?.seekToNext() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next",
                        )
                    }
                }
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(metadata?.artworkUri) // 使用metadata中的URI
                    .crossfade(true)
                    .build(),
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
                    .clickable(onClick = {
                        // 启动 NowPlayingActivity
                        context.startActivity(
                            Intent(
                                context,
                                NowPlayingActivity::class.java,
                            ),
                        )
                    }),
                onSuccess = { result ->
                    themeColor = ImageParser.imageThemeColor(result.result.drawable.toBitmap())
//                    textColor = ImageParser.getContrastingTextColor(textColor)
                    if (BuildConfig.DEBUG) {
                        Log.d("AlbumArt", "Image theme color: $themeColor")
                    }
                },
                onError = { error ->
                    if (BuildConfig.DEBUG) {
                        Log.e("AlbumArt", "Image load failed: ${error.result.throwable.message}")
                    }
                },
            )
        }
    }
}
