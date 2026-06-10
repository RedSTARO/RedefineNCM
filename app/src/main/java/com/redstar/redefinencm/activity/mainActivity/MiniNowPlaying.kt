package com.redstar.redefinencm.activity.mainActivity

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.NowPlayingActivity
import com.redstar.redefinencm.util.ImageParser
import com.redstar.redefinencm.viewmodel.MainViewModel

@Composable
fun MiniNowPlaying(context: Context, viewModel: MainViewModel) {
    val metadata by viewModel.nowPlayingMetadata.collectAsState()
    val isPlaying by viewModel.nowPayingIsPlaying.collectAsState()
    val mediaController by viewModel.mediaController.collectAsState()
    val defaultContainerColor = MaterialTheme.colorScheme.primaryContainer
    var themeColor by remember { mutableStateOf(defaultContainerColor) }
    val containerColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = spring(),
        label = "miniPlayerColor",
    )
    val contentColor = if (containerColor.luminance() > 0.5f) Color.Black else Color.White

    Surface(
        modifier = Modifier
            .size(width = 300.dp, height = 112.dp)
            .padding(16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 4.dp,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(metadata?.artworkUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable(onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    NowPlayingActivity::class.java,
                                ),
                            )
                        }),
                    onSuccess = { result ->
                        themeColor = ImageParser.imageThemeColor((result.result.image as coil3.BitmapImage).bitmap)
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

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = (metadata?.title ?: "Not Playing").toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(),
                    )
                    Text(
                        text = (metadata?.artist ?: "").toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { mediaController?.seekToPrevious() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous",
                            )
                        }
                        FilledIconButton(
                            onClick = {
                                if (isPlaying) mediaController?.pause() else mediaController?.play()
                            },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = contentColor.copy(alpha = 0.18f),
                                contentColor = contentColor,
                            ),
                        ) {
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
            }
        }
    }
}
