package com.redstar.redefinencm.activity

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.services.LyricCallback
import com.redstar.redefinencm.services.playbackService
import com.redstar.redefinencm.services.setLyricCallback
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
    val metadataFlow = remember { MutableStateFlow<MediaMetadata?>(null) }
    val metadata by metadataFlow.collectAsState()
    var themeColor by remember { mutableStateOf(Color.Gray) }
    var currentLyric by remember { mutableStateOf<String>("") }

    LaunchedEffect(Unit) {
        val sessionToken =
            SessionToken(context, ComponentName(context, playbackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
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

    // 设置歌词更新的回调
    setLyricCallback(object : LyricCallback {
        override fun onLyricUpdated(lyric: String, duration: Int) {
            currentLyric = lyric // 更新当前歌词
        }
    })

    Card(
        shape = RoundedCornerShape(24.dp), // 更大圆角，更现代
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // 增强阴影
        modifier = Modifier
            .fillMaxWidth(0.9f) // 稍微增加宽度占比
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
                    themeColor = ImageParser().imageThemeColor(result.result.drawable.toBitmap())
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

            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一首
                PlaybackButton(
                    onClick = { mediaController?.seekToPrevious() },
                    icon = painterResource(android.R.drawable.ic_media_previous), // TODO: 替换图标
                    contentDescription = "上一首"
                )

                // 播放/暂停
                PlaybackButton(
                    onClick = { if (mediaController?.isPlaying == true) mediaController?.pause() else mediaController?.play() },
                    icon = painterResource(
                        if (mediaController?.isPlaying == true) android.R.drawable.ic_media_pause
                        else android.R.drawable.ic_media_play
                    ),
                    contentDescription = "播放/暂停",
                    modifier = Modifier.size(64.dp), // 更大按钮突出主操作
                    containerColor = MaterialTheme.colorScheme.primary
                )

                // 下一首
                PlaybackButton(
                    onClick = { mediaController?.seekToNext() },
                    icon = painterResource(android.R.drawable.ic_media_next),
                    contentDescription = "下一首"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 次级按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* TODO: 添加到喜欢 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("喜欢", color = MaterialTheme.colorScheme.onSecondary) }

                Button(
                    onClick = {
                        mediaController?.setShuffleModeEnabled(!mediaController?.shuffleModeEnabled!!)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("随机", color = MaterialTheme.colorScheme.onSecondary) }
            }

            // 歌词显示
            Text(
                text = currentLyric, // 显示当前歌词
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )


        }
    }
}

@Composable
fun PlaybackButton(
    onClick: () -> Unit,
    icon: Painter,
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
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}
