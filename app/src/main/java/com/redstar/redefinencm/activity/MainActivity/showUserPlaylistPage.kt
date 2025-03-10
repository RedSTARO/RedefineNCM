package com.redstar.redefinencm.activity.MainActivity

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.data.*
import kotlinx.coroutines.launch

@Composable
fun showUserPlaylistPage(retrofit: NCMApi, uid: Long,navController: NavController, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var userPlaylist by remember { mutableStateOf<userPlaylist?>(null) }
    var playlist by remember { mutableStateOf(emptyList<userPlaylistEach>()) }
    var userDetail by remember { mutableStateOf<userDetail?>(null) }
    val scrollState = rememberScrollState() // 监听滚动状态


    // 异步加载用户的播放列表
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userPlaylist = retrofit.userPlaylist(uid)
            userDetail = retrofit.userDetail(uid)
            Log.d("showUserPlaylistPage", userDetail!!.code.toString() + userDetail!!.profile)
            Log.d("showUserPlaylistPage", userPlaylist!!.code.toString() + userPlaylist!!.playlist)
            playlist = userPlaylist!!.playlist
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // 背景固定高度，避免拉伸
        ) {
            AsyncImage(
                model = userDetail?.profile?.backgroundUrl,
                contentDescription = "User Background",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop // 确保图片填充整个 Box

            )

            // 半透明遮罩，提升文字可读性
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = userDetail?.profile?.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )

                Text(
                    text = userDetail?.profile?.nickname ?: "Unknown User",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "ID: ${userDetail?.profile?.userId ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(playlist) { userPlaylistEach ->
                playlistCard(
                    userPlaylistEach,
                    when {
                        userPlaylistEach.name.contains("喜欢的音乐") -> "fav"
                        userPlaylistEach.name.contains("私人雷达") -> "radar"
                        else -> "no"
                    },
                    navController,
                    modifier
                )
            }
        }
    }

}

@Composable
fun playlistCard(userPlaylistEach: userPlaylistEach,specialCard: String, navController: NavController, modifier: Modifier = Modifier){
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp) // 设置卡片之间的间距
            .fillMaxWidth(), // 设置宽度填充父容器
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // 提升卡片阴影
        shape = RoundedCornerShape(16.dp), // 圆角
        onClick = {
            navController.navigate("playlistDetailPage/${userPlaylistEach.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp) // 卡片内的填充
                .fillMaxWidth(), // 填满父容器宽度
            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
        ) {
            // 显示用户头像，设置圆形裁剪
            AsyncImage(
                model = userPlaylistEach.creator.avatarUrl,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(50.dp) // 设置头像大小
                    .clip(CircleShape) // 设置圆形裁剪
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) // 边框
            )

            Spacer(modifier = Modifier.width(16.dp)) // 头像和文本之间的间距

            // 显示播放列表名称
            Column(
                modifier = Modifier.weight(1f) // 让文本占据剩余空间
            ) {
                Text(
                    text = userPlaylistEach.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground // 设置文本颜色
                )
                // 可选的副标题或描述
                Text(
                    text = userPlaylistEach.creator.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // 设置副标题颜色
                )
            }
            // TODO: Special cards
            if (specialCard == "fav"){
                Text(text = "心动模式todo")
            }
            else if (specialCard == "radar"){
                Text(text = "私人雷达todo")
            }
        }
    }
}