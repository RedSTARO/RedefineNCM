package com.redstar.redefinencm.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import coil.compose.AsyncImage
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.data.api.data.CommentMusicComments
import com.redstar.redefinencm.data.api.data.UserDetailProfile
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.ImageParser
import com.redstar.redefinencm.viewmodel.NowPlayingViewModel
import com.skydoves.cloudy.cloudy

class NowPlayingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: NowPlayingViewModel = viewModel()
            RedefineNCMTheme {
                Scaffold(
                    topBar = {
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            NowPlaying(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NowPlaying(viewModel: NowPlayingViewModel) {
    val metadata by viewModel.nowPlayingMetadata.collectAsState()
    val commentsEntity by viewModel.comments.collectAsState()
    val currentIndex by viewModel.currentMediaIndexInList.collectAsState()
    val lyricMap by viewModel.lyricMap.collectAsState()
    val lyricIndex by viewModel.lyricIndex.collectAsState()
    var showPlaylist by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var currentRandomStatus by remember { mutableStateOf(false) }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.currentPosition.collectAsState()
    val songLength by viewModel.songLength.collectAsState()
    val playList by viewModel.playList.collectAsState()

    Column {
        SongDetails(metadata, onShowLyricClick = { })
        Lyric(
            lyricMap = lyricMap,
            lyricIndex = lyricIndex,
        )
        ProgressBar(
            currentPosition = position,
            songLength = songLength,
            onSeekChanged = { viewModel.onPositionSeekClick(it) },
        )
        PlaybackControlButtons(
            isPlaying = isPlaying,
            onFavClick = { viewModel.onFavClick() },
            onPervClick = { viewModel.onPervClick() },
            onPauseClick = { viewModel.onPauseClick() },
            onNextClick = { viewModel.onNextClick() },
            onShowPlaylistClick = { showPlaylist = !showPlaylist },
            currentRandomStatus = currentRandomStatus,
            onShuffleClick = {
                currentRandomStatus = !currentRandomStatus
                viewModel.onShuffleClick(currentRandomStatus)
            },
            onCommentsClick = {
                if (!showComments) {
                    viewModel.getComments()
                }
                showComments = !showComments
            },
            currentFavStatus = false,
            modifier = Modifier,
        )
    }

    if (showPlaylist) {
        viewModel.onPlaylistClick()
        CurrentPlayList(
            playlist = playList,
            onDismiss = { showPlaylist = false },
            currentIndex = currentIndex?.toInt() ?: 0,
            onSeekClick = { viewModel.onSeekClick(it) },
        )
    }

    if (showComments) {
        Comments(commentsEntity.hotComments, onDismiss = { showComments = false })
    }
}

@Composable
fun SongDetails(metadata: MediaMetadata?, onShowLyricClick: () -> Unit) {
    var themeColor by remember { mutableStateOf(Color.Gray) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.BottomStart,
    ) {
        AsyncImage(
            model = metadata?.artworkUri,
            contentDescription = "专辑封面",
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { onShowLyricClick() }),
            onSuccess = { result ->
                themeColor = ImageParser.imageThemeColor(result.result.drawable.toBitmap())
                if (BuildConfig.DEBUG) {
                    Log.d("AlbumArt", "Image theme color: $themeColor")
                }
            },
            onError = { error ->
                if (BuildConfig.DEBUG) {
                    Log.e("AlbumArt", "Image load failed: ${error.result.throwable.message}")
                }
                themeColor = Color.Gray
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
                )
                .padding(12.dp),
        ) {
            Text(
                text = metadata?.title?.toString() ?: "未知标题",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
            )
            Text(
                text = metadata?.artist?.toString() ?: "未知艺术家",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
            )
        }
    }
}

@Composable
fun Lyric(lyricMap: LinkedHashMap<Long?, String?>, lyricIndex: Int) {
    val listState = rememberLazyListState()
    val lyrics = lyricMap.values.toList()

    LaunchedEffect(lyricIndex) {
        if (lyricIndex >= 0 && lyricIndex < lyrics.size) {
            listState.animateScrollToItem(lyricIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isCurrent = index == lyricIndex
            Text(
                text = line.toString(),
                fontSize = if (isCurrent) 20.sp else 16.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .alpha(if (isCurrent) 1f else 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ProgressBar(
    currentPosition: Long,
    songLength: Long,
    onSeekChanged: (Long) -> Unit,
) {
    val progress = if (songLength > 0) {
        currentPosition.toFloat() / songLength.toFloat()
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
    ) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        )

        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { percent ->
                onSeekChanged((percent * songLength).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        )
    }
}

@Composable
fun PlaybackControlButtons(
    isPlaying: Boolean,
    onFavClick: () -> Unit,
    onPervClick: () -> Unit,
    onPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onShowPlaylistClick: () -> Unit,
    currentRandomStatus: Boolean,
    onShuffleClick: () -> Unit,
    onCommentsClick: () -> Unit,
    currentFavStatus: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = if (currentRandomStatus) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (currentRandomStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onPervClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous",
                modifier = Modifier.size(36.dp),
            )
        }
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                modifier = Modifier.size(48.dp),
            )
        }
        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                modifier = Modifier.size(36.dp),
            )
        }
        IconButton(onClick = onShowPlaylistClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Playlist",
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        IconButton(onClick = onFavClick) {
            Icon(
                imageVector = if (currentFavStatus) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (currentFavStatus) Color.Red else MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onCommentsClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Comment,
                contentDescription = "Comments",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentPlayList(
    playlist: List<MediaItem>,
    onDismiss: () -> Unit,
    currentIndex: Int,
    onSeekClick: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            itemsIndexed(playlist) { index, item ->
                val isCurrent = index == currentIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSeekClick(index)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(32.dp),
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                    )
                    Column {
                        Text(
                            text = item.mediaMetadata.title?.toString() ?: "未知标题",
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        )
                        Text(
                            text = item.mediaMetadata.artist?.toString() ?: "未知艺术家",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comments(comments: List<CommentMusicComments>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            itemsIndexed(comments) { _, comment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                ) {
                    AsyncImage(
                        model = comment.user.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Gray, RoundedCornerShape(20.dp)),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = comment.user.nickname,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = comment.content,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = comment.timeStr,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NowPlayingPreview() {
    RedefineNCMTheme {
        ProgressBar(35, 100L, {})
    }
}

@Preview(showBackground = true)
@Composable
fun CommentsPreview() {
    val commentMusicComments = listOf(
        CommentMusicComments(
            user = UserDetailProfile(nickname = "User 1", avatarUrl = "", userId = 0, backgroundUrl = ""),
            commentId = 1,
            content = "This is a comment",
            richContent = "",
            time = 0,
            timeStr = "2023-01-01",
            likedCount = 0,
        ),
    )
    RedefineNCMTheme {
        Comments(commentMusicComments, {})
    }
}
