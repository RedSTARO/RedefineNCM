package com.redstar.redefinencm.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.services.PlaybackService
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.ImageParser
import com.redstar.redefinencm.viewmodel.NowPlayingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NowPlayingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: NowPlayingViewModel = viewModel()
            LaunchedEffect(Unit) {
                PlaybackService.LyricBridge.viewModel = viewModel
            }
            RedefineNCMTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "Now Playing",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,

                        ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            SongDetails(viewModel, modifier = Modifier.fillMaxWidth())
                            Lyric(viewModel)
                            PlaybackControlButtons(viewModel, modifier = Modifier.fillMaxWidth())
                            PlaylistButtons(viewModel, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongDetails(viewModel: NowPlayingViewModel, modifier: Modifier = Modifier) {
    RedefineNCMApplication.getApplicationContext() as Context
    var themeColor by remember { mutableStateOf(Color.Gray) }
    val metadata by viewModel.nowPlayingMetadata.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp) // 增加内边距
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 专辑封面
            AsyncImage(
                model = metadata?.artworkUri,
                contentDescription = "专辑封面",
                modifier = Modifier
                    .size(200.dp) // 固定大小，突出封面
                    .clip(RoundedCornerShape(12.dp)) // 圆角封面
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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

            Spacer(modifier = Modifier.height(16.dp))

            // 歌曲信息
            Text(
                text = metadata?.title?.toString() ?: "未知标题",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = metadata?.artist?.toString() ?: "未知艺术家",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun Lyric(viewModel: NowPlayingViewModel) {
    val lyricMap by viewModel.lyricMap.collectAsState()
    val lyricIndex by viewModel.lyricIndex.collectAsState()
    val listState = rememberLazyListState()

    val lyrics = remember(lyricMap) { lyricMap.values.toList() }

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
            .height(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlaybackControlButtons(viewModel: NowPlayingViewModel, modifier: Modifier = Modifier) {
    val isPlaying by viewModel.nowPayingIsPlaying.collectAsState()
    val mediaController by viewModel.mediaController.collectAsState()


    // Control buttons
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Perv
        PlaybackButton(
            viewModel,
            onClick = { mediaController?.seekToPrevious() },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "上一首",
        )

        // Pause
        PlaybackButton(
            viewModel,
            onClick = {
                if (isPlaying) mediaController?.pause() else mediaController?.play()
            },
            icon = if (isPlaying) Icons.Default.Home else Icons.Default.PlayArrow,
            contentDescription = "播放/暂停",
            modifier = Modifier.size(64.dp), // 更大按钮突出主操作
            containerColor = MaterialTheme.colorScheme.primary,
        )

        // Next
        PlaybackButton(
            viewModel,
            onClick = { mediaController?.seekToNext() },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "下一首",
        )
    }
}

@Composable
fun PlaylistButtons(viewModel: NowPlayingViewModel, modifier: Modifier = Modifier) {
    var showPlaylist by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    val repeatModes = mapOf(
        1 to Player.REPEAT_MODE_OFF,
        2 to Player.REPEAT_MODE_ONE,
        0 to Player.REPEAT_MODE_ALL,
    )
    var currentRepeatStatus by remember { mutableStateOf(0) }
    val mediaController by viewModel.mediaController.collectAsState()

    Column {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            FuncButton(
                onClick = { showPlaylist = true },
                text = "Play list",
            )
            if (showPlaylist) {
                CurrentPlayList(viewModel, onDismiss = { showPlaylist = false })
            }

            FuncButton(
                onClick = { mediaController?.setShuffleModeEnabled(!mediaController?.shuffleModeEnabled!!) },
                text = "Random",
            )

            FuncButton(
                onClick = {
                    mediaController?.setRepeatMode(repeatModes[(currentRepeatStatus++)]!!)
                    currentRepeatStatus = (currentRepeatStatus) % 3
                    if (BuildConfig.DEBUG) {
                        Log.d("RepeatMode", "Repeat mode: $currentRepeatStatus")
                    }
                },
                text = "Repeat Mode",
            )


        }
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FuncButton(
                onClick = {
                    val mediaId = mediaController?.currentMediaItem?.mediaId
                    CoroutineScope(Dispatchers.IO).launch {
                        safeApiCall {
                            RetrofitInstance.retrofit.create(NCMApi::class.java)
                                .like(mediaId?.toLong())
                        }
                    }
                },
                text = "Fav",
            )

            FuncButton(
                onClick = {
                    showComments = true
                },
                text = "Comments",
            )

            if (showComments) {
                Comments(viewModel, onDismiss = { showComments = false })
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentPlayList(viewModel: NowPlayingViewModel, onDismiss: () -> Unit) {
    val mediaController by viewModel.mediaController.collectAsState()
    var playlist by remember { mutableStateOf<List<MediaItem>?>(null) }
    var currentMediaId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()


    LaunchedEffect(Unit) {
        val mediaItemCount = mediaController?.mediaItemCount ?: 0
        val mediaItems = mutableListOf<MediaItem>()
        for (i in 0 until mediaItemCount) {
            mediaController?.getMediaItemAt(i)?.let { mediaItems.add(it) }
        }
        playlist = mediaItems

        mediaController!!.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaId = mediaItem?.mediaId
            }
        })
        currentMediaId = mediaController!!.currentMediaItem?.mediaId

        val currentIndex = playlist?.indexOfFirst { it.mediaId == currentMediaId } ?: -1
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    if (!playlist.isNullOrEmpty()) {
        ModalBottomSheet(onDismissRequest = { onDismiss() }) {
            LazyColumn(state = listState) {
                itemsIndexed(playlist ?: emptyList()) { index, item ->
                    Row {
                        Spacer(Modifier.padding(5.dp))
                        Card(
                            modifier = Modifier
                                .clickable {
                                    mediaController?.seekTo(index, 0)
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
                    Spacer(Modifier.padding(5.dp))
                    if (BuildConfig.DEBUG) {
                        Log.d("Playlist", "Item $index: ${item.mediaMetadata.title}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comments(viewModel: NowPlayingViewModel, onDismiss: () -> Unit) {
    val mediaController by viewModel.mediaController.collectAsState()
    val comments by viewModel.comments.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getComments(mediaController!!.currentMediaItem!!.mediaId.toLong())
    }

    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        LazyColumn {
            itemsIndexed(comments.hotComments) { index, item ->
                Text(text = "$index, ${item.user.nickname}: ${item.content}")
            }
        }
    }
}

@Composable
fun PlaybackButton(
    viewModel: NowPlayingViewModel,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(48.dp), // 默认大小
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp),
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

