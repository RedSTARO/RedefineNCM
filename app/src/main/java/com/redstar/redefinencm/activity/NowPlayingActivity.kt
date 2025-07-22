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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.redstar.redefinencm.data.db.entity.CommentMusicEntity
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.ImageParser
import com.redstar.redefinencm.viewmodel.NowPlayingViewModel

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
//                        contentAlignment = Alignment.Center,

                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
//                            horizontalAlignment = Alignment.CenterHorizontally,
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
    val mediaController by viewModel.mediaController.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val currentIndex by viewModel.currentMediaIndexInList.collectAsState()
    val lyricMap by viewModel.lyricMap.collectAsState()
    val lyricIndex by viewModel.lyricIndex.collectAsState()
    var showPlaylist by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var currentRandomStatus by remember { mutableStateOf(false) }
    var showLyric by remember { mutableStateOf(false) }
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
                viewModel.getComments()
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
        Comments(comments, onDismiss = { showComments = false })
    }
}

@Composable
fun SongDetails(metadata: MediaMetadata?, onShowLyricClick: () -> Unit) {
    var themeColor by remember { mutableStateOf(Color.Gray) }

    // 图片 + 文本层叠
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.BottomStart,
    ) {
        // 专辑封面
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

        // 悬浮的文字信息（带背景半透明遮罩）
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

    // 自动滚动到当前歌词行
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        // 背后是漂亮的波浪进度条
        LinearWavyProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        )

        // 透明 Slider 覆盖在上面处理拖动
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
    onFavClick: () -> Unit,
    onPervClick: () -> Unit,
    onPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onShowPlaylistClick: () -> Unit,
    currentRandomStatus: Boolean,
    onShuffleClick: () -> Unit,
    onCommentsClick: () -> Unit,
    currentFavStatus: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    var currentFavStatus by remember { mutableStateOf(currentFavStatus) }
    // Control buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton_(
            onClick = {
                onFavClick()
                currentFavStatus = !currentFavStatus // TODO: Really do remove fav
            },
            icon = if (currentFavStatus) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Like this music",
        )

        // Perv
        IconButton_(
            onClick = { onPervClick() },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "上一首",
        )

        // Pause
        IconButton_(
            onClick = {
                onPauseClick()
            },
            icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "播放/暂停",
            modifier = Modifier.size(64.dp),
        )

        // Next
        IconButton_(
            onClick = { onNextClick() },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "下一首",
        )

        IconButton_(
            onClick = {
                onShowPlaylistClick()
            },
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = "Current playlist",
        )
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton_(
                onClick = {
                    onShuffleClick()
                },
                icon = if (!currentRandomStatus) Icons.Filled.Shuffle else Icons.Filled.ShuffleOn,
                contentDescription = "Shuffle: $currentRandomStatus",
            )

//            FuncButton(
//                onClick = {
//                    mediaController?.setRepeatMode(repeatModes[(currentRepeatStatus++)]!!)
//                    currentRepeatStatus = (currentRepeatStatus) % 3
//                    if (BuildConfig.DEBUG) {
//                        Log.d("RepeatMode", "Repeat mode: $currentRepeatStatus")
//                    }
//                },
//                text = "Repeat Mode",
//            )

            IconButton_(
                onClick = {
                    onCommentsClick()
                },
                contentDescription = "Comments",
                icon = Icons.AutoMirrored.Filled.Comment,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentPlayList(playlist: List<MediaItem?>, currentIndex: Int, onSeekClick: (Int) -> Unit, onDismiss: () -> Unit) {
    val currentMediaId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    if (playlist.isNotEmpty()) {
        ModalBottomSheet(onDismissRequest = { onDismiss() }) {
            LazyColumn(state = listState) {
                itemsIndexed(playlist) { index, item ->
                    Row {
                        Spacer(Modifier.padding(5.dp))
                        if (item != null) {
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        onSeekClick(index)
                                    }
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentMediaId == item.mediaId) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    } else {
                                        Color.Transparent
                                    },
                                ),
                            ) {
                                Text(
                                    text = "$index: ${item.mediaMetadata.title}",
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally),
                                )
                                Text(
                                    text = item.mediaMetadata.artist.toString(),
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.padding(5.dp))
                    if (BuildConfig.DEBUG) {
                        Log.d("Playlist", "Item $index: ${item?.mediaMetadata?.title}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comments(comments: CommentMusicEntity, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        LazyColumn {
            itemsIndexed(comments.hotComments) { index, item ->
                Text(text = "$index, ${item.user.nickname}: ${item.content}")
            }
        }
    }
}

@Composable
fun IconButton_(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(50.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(35.dp), // This must be less than above
        )
    }
}

@Composable
fun FuncButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(12.dp),
    ) { Text(text, color = MaterialTheme.colorScheme.onSecondary) }
}

@Preview
@Composable
fun NowPlayingPreview() {
    val metadata = MediaMetadata.Builder()
        .setTitle("SongName")
        .setArtist("SongArtist")
        .build()
    val lyricMap = mutableMapOf<Long?, String?>()
    lyricMap[0] = "LyricLine"
    lyricMap[1] = "LyricLine1"
    lyricMap[2] = "LyricLine2"
    Column {
        SongDetails(metadata, onShowLyricClick = {})
        Lyric(
            lyricMap = lyricMap as LinkedHashMap<Long?, String?>,
            lyricIndex = 0,
        )
        ProgressBar(35, 100L, {})
        PlaybackControlButtons(
            onFavClick = {},
            onPervClick = {},
            onPauseClick = {},
            onNextClick = {},
            onShowPlaylistClick = {},
            currentRandomStatus = false,
            onShuffleClick = {},
            onCommentsClick = {},
            isPlaying = false,
            modifier = Modifier,
            currentFavStatus = false,
        )
    }
}

@Preview
@Composable
fun CommentsPreview() {
    val CommentMusicEntity = CommentMusicEntity(
        id = 0,
        isMusician = false,
        userId = 0,
        topComments = listOf(
            CommentMusicComments(
                user = UserDetailProfile(
                    avatarUrl = "",
                    nickname = "NickName",
                    backgroundUrl = "",
                    userId = 0,
                ),
                commentId = 0,
                content = "CommentContent",
                richContent = "",
                time = 0,
                timeStr = "TimeOfComment",
                likedCount = 99,
            ),
        ),
        moreHot = false,
        hotComments = listOf(
            CommentMusicComments(
                user = UserDetailProfile(
                    avatarUrl = "",
                    nickname = "NickName",
                    backgroundUrl = "",
                    userId = 0,
                ),
                commentId = 0,
                content = "CommentContent",
                richContent = "",
                time = 0,
                timeStr = "TimeOfComment",
                likedCount = 99,
            ),
        ),
    )

    Comments(CommentMusicEntity, {})
}
