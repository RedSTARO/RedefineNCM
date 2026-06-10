package com.redstar.redefinencm.activity.mainActivity

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.data.api.data.SongDetailSongs
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.ui.component.connectedListItemShape
import com.redstar.redefinencm.util.DownloadUtil
import com.redstar.redefinencm.util.ImageParser
import com.redstar.redefinencm.util.SettingProvider
import com.redstar.redefinencm.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ShowPlaylistDetailPage(
    viewModel: MainViewModel,
    songlistID: Long,
) {
    val retrofit = viewModel.retrofit
    var themeColor by remember { mutableStateOf(Color.Gray) }
    // Expressive motion: the hero colour springs into place when the cover loads.
    val heroColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = spring(),
        label = "heroColor",
    )

    val playlistDetail by viewModel.playlistDetail.collectAsState()
    val playlistSongs by viewModel.playlistSongs.collectAsState()
    val replacePlaylist = remember { SettingProvider.replacePlaylist }

    LaunchedEffect(songlistID) {
        viewModel.fetchPlaylistDetail(songlistID)
    }

    val songs = playlistSongs?.songs ?: emptyList()
    val trackCountText = when {
        playlistDetail?.playlist?.trackCount == 0L -> (playlistSongs?.songs?.size ?: 0).toString()
        playlistDetail?.playlist?.trackCount == null -> "未知"
        else -> playlistDetail?.playlist?.trackCount.toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        item {
            PlaylistHeader(
                coverUrl = playlistDetail?.playlist?.coverImgUrl,
                title = playlistDetail?.playlist?.name ?: "加载中...",
                trackCountText = trackCountText,
                heroColor = heroColor,
                onCoverColor = { themeColor = it },
                onPlayAll = { viewModel.onPlaySingleSongInPlaylistClick(songlistID, 0) },
                onDownloadAll = { viewModel.onDownloadPlaylistClick(songlistID) },
            )
        }

        itemsIndexed(songs) { index, song ->
            SongListItem(
                index = index,
                song = song,
                shape = connectedListItemShape(index, songs.size),
                onClick = {
                    if (BuildConfig.DEBUG) {
                        Log.d("showPlaylistDetail", "Selected Song ${song.name} with id ${song.id}")
                    }
                    if (replacePlaylist) {
                        viewModel.onPlaySingleSongInPlaylistClick(playlistDetail!!.id, song.id)
                    } else {
                        viewModel.onPlaySingleSongClick(song)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        safeApiCall { retrofit.playlistUpdatePlaycount(songlistID) }
                    }
                },
            )
        }

        item { Spacer(Modifier.height(96.dp)) } // room for the mini player FAB
    }
}

/**
 * Aggressive M3-Expressive hero: a vibrant album-colour gradient band behind a large,
 * heavily-rounded cover, big display title, and a prominent pill "play all" button.
 */
@Composable
private fun PlaylistHeader(
    coverUrl: String?,
    title: String,
    trackCountText: String,
    heroColor: Color,
    onCoverColor: (Color) -> Unit,
    onPlayAll: () -> Unit,
    onDownloadAll: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            heroColor,
                            heroColor.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = "Playlist Cover",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(200.dp)
                    .clip(RoundedCornerShape(36.dp)),
                onSuccess = { result ->
                    onCoverColor(ImageParser.imageThemeColor((result.result.image as coil3.BitmapImage).bitmap))
                },
                onError = {
                    onCoverColor(Color.Gray)
                    if (BuildConfig.DEBUG) {
                        Log.e("AlbumArt", "Image load failed: ${it.result.throwable.message}")
                    }
                },
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                ),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$trackCountText 首歌曲",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = CircleShape,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("播放全部", style = MaterialTheme.typography.titleMedium)
                }
                FilledTonalIconButton(
                    onClick = onDownloadAll,
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "下载全部")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * One row in the MD3-Expressive "connected list": consecutive items share a tonal block with
 * large outer corners and tight inner corners (computed by [groupedItemShape]).
 */
@Composable
private fun SongListItem(
    index: Int,
    song: SongDetailSongs,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.5.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = (index + 1).toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(36.dp),
            )

            AsyncImage(
                model = song.al.picUrl,
                contentDescription = "Album Cover",
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium),
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
                Text(
                    text = song.ar.joinToString(" / ") { it.name }.ifEmpty { "未知歌手" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (SettingProvider.showDownloadStatus) {
                val downloaded = DownloadUtil.fileAlreadyExistsByBaseName(song.id.toString())
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = if (downloaded) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Icon(
                        imageVector = if (downloaded) Icons.Filled.Check else Icons.Filled.AttachFile,
                        contentDescription = if (downloaded) "Downloaded" else "Not downloaded",
                        tint = if (downloaded) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(6.dp).size(18.dp),
                    )
                }
            }
        }
    }
}
