package com.redstar.redefinencm.activity

import android.content.ComponentName
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.services.playbackService
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.ImageParser
import kotlinx.coroutines.flow.MutableStateFlow
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
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "Now Playing",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center

                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SongDetails(modifier = Modifier.fillMaxWidth())
                            PlaybackControlButtons(modifier = Modifier.fillMaxWidth())
                            PlaylistButtons(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongDetails(modifier: Modifier = Modifier) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val applicationContext = RedefineNCMApplication.getApplicationContext() as Context
    val metadataFlow = remember { MutableStateFlow<MediaMetadata?>(null) }
    val metadata by metadataFlow.collectAsState()
    var themeColor by remember { mutableStateOf(Color.Gray) }

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
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColor
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp) // 增加内边距
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    Log.d("AlbumArt", "Image theme color: $themeColor")
                },
                onError = { error ->
                    Log.e("AlbumArt", "Image load failed: ${error.result.throwable.message}")
                    themeColor = Color.Gray
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 歌曲信息
            Text(
                text = metadata?.title?.toString() ?: "未知标题",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = metadata?.artist?.toString() ?: "未知艺术家",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlaybackControlButtons(modifier: Modifier = Modifier) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val applicationContext = RedefineNCMApplication.getApplicationContext() as Context
    var isPlaying by remember { mutableStateOf(mediaController?.isPlaying == true) }

    LaunchedEffect(Unit) {
        val sessionToken =
            SessionToken(
                applicationContext,
                ComponentName(applicationContext, playbackService::class.java)
            )
        val controllerFuture =
            MediaController.Builder(applicationContext, sessionToken).buildAsync()
        mediaController = controllerFuture.await()
    }

    // Control buttons
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Perv
        PlaybackButton(
            onClick = { mediaController?.seekToPrevious() },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "上一首"
        )

        // Pause
        PlaybackButton(
            onClick = {
                isPlaying = mediaController?.isPlaying == true
                if (isPlaying) mediaController?.pause() else mediaController?.play()
            },
            icon = if (isPlaying) Icons.Default.Home else Icons.Default.PlayArrow,
            contentDescription = "播放/暂停",
            modifier = Modifier.size(64.dp), // 更大按钮突出主操作
            containerColor = MaterialTheme.colorScheme.primary
        )

        // Next
        PlaybackButton(
            onClick = { mediaController?.seekToNext() },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "下一首"
        )
    }
}

@Composable
fun PlaylistButtons(modifier: Modifier = Modifier) {
    var showPlaylist by remember { mutableStateOf(false) }
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val applicationContext = RedefineNCMApplication.getApplicationContext() as Context

    LaunchedEffect(Unit) {
        val sessionToken =
            SessionToken(
                applicationContext,
                ComponentName(applicationContext, playbackService::class.java)
            )
        val controllerFuture =
            MediaController.Builder(applicationContext, sessionToken).buildAsync()
        mediaController = controllerFuture.await()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            onClick = { /* TODO: 添加到喜欢 */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) { Text("喜欢", color = MaterialTheme.colorScheme.onSecondary) }

        Button(
            onClick = { showPlaylist = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Playlist", color = MaterialTheme.colorScheme.onSecondary) }

        Button(
            onClick = {
                mediaController?.setShuffleModeEnabled(!mediaController?.shuffleModeEnabled!!)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) { Text("随机", color = MaterialTheme.colorScheme.onSecondary) }

        if (showPlaylist) {
            CurrentPlayList(onDismiss = { showPlaylist = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentPlayList(onDismiss: () -> Unit) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val applicationContext = RedefineNCMApplication.getApplicationContext() as Context
    var playlist by remember { mutableStateOf<List<MediaItem>?>(null) }
    val currentMediaId = mediaController?.currentMediaItem?.mediaId

    LaunchedEffect(Unit) {
        val sessionToken = SessionToken(
            applicationContext,
            ComponentName(applicationContext, playbackService::class.java)
        )
        val controllerFuture =
            MediaController.Builder(applicationContext, sessionToken).buildAsync()
        mediaController = controllerFuture.await()

        val mediaItemCount = mediaController?.mediaItemCount ?: 0
        val mediaItems = mutableListOf<MediaItem>()
        for (i in 0 until mediaItemCount) {
            mediaController?.getMediaItemAt(i)?.let { mediaItems.add(it) }
        }
        playlist = mediaItems
    }
    if (!playlist.isNullOrEmpty()) {
        ModalBottomSheet(onDismissRequest = { onDismiss() }) {
            LazyColumn {
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
                                containerColor = if (currentMediaId == item.mediaId)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else
                                    Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (currentMediaId == item.mediaId) 8.dp else 2.dp
                            )
                        )
                        {
                            Text(
                                text = "$index: ${item.mediaMetadata.title}",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = item.mediaMetadata.artist.toString(),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    Spacer(Modifier.padding(5.dp))
                    Log.d("Playlist", "Item $index: ${item.mediaMetadata.title}")
                }
            }
        }
    }
}


@Composable
fun PlaybackButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(48.dp), // 默认大小
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}
