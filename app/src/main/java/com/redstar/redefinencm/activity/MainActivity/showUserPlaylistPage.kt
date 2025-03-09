package com.redstar.redefinencm.activity.MainActivity

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.data.*
import kotlinx.coroutines.launch

@Composable
fun showUserPlaylistPage(retrofit: NCMApi, uid: Long,navController: NavController, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var playlist by remember { mutableStateOf(emptyList<userPlaylistEach>()) }

    // 异步加载用户的播放列表
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userPlaylist = retrofit.userPlaylist(uid)
            Log.d("showUserPlaylistPage", userPlaylist.code.toString() + userPlaylist.more + userPlaylist.playlist)
            playlist = userPlaylist.playlist
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(playlist) { userPlaylistEach ->
            Log.d("showUserPlaylistPage", userPlaylistEach.name + userPlaylistEach.id)
            if (userPlaylistEach.name.contains("喜欢的音乐")) {
                Log.d("showUserPlaylistPage", "Hit SP List: ${userPlaylistEach.name}")
                playlistCard(userPlaylistEach, "fav", navController, modifier)
            }else if(userPlaylistEach.name.contains("私人雷达")) {
                Log.d("showUserPlaylistPage", "Hit SP List: ${userPlaylistEach.name}")
                playlistCard(userPlaylistEach, "radar", navController, modifier)
            }
            else {
                playlistCard(userPlaylistEach, "no", navController, modifier)
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