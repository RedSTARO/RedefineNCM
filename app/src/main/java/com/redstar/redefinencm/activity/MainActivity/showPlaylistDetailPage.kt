package com.redstar.redefinencm.activity.MainActivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.data.*



@Composable
fun showPlaylistDetailPage(songlistID: Long, retrofit: NCMApi,navController: NavController, modifier: Modifier = Modifier) {
    var playlistDetail by remember { mutableStateOf<playlistDetail?>(null) }
    var playlistSongs by remember { mutableStateOf<playlistTrackAll?>(null) }
//    var playUrls by remember { mutableStateOf<songUrlV1?>(null) }
    LaunchedEffect(Unit) {
        playlistDetail = retrofit.playlistDetail(songlistID)
        playlistSongs = retrofit.playlistTrackAll(songlistID)
    }
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // 增加外边距
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // 提升阴影
            shape = RoundedCornerShape(12.dp) // 圆角
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // 增加内边距
                horizontalAlignment = Alignment.CenterHorizontally // 水平居中
            ) {
                // 专辑封面
                AsyncImage(
                    model = playlistDetail?.playlist?.coverImgUrl,
                    contentDescription = "Playlist Cover",
                    modifier = Modifier
                        .size(120.dp) // 适当加大封面
                        .clip(RoundedCornerShape(12.dp)) // 让封面有圆角
                )

                Spacer(modifier = Modifier.height(12.dp)) // 适当增加间距

                // 歌单名称
                Text(
                    text = playlistDetail?.playlist?.name ?: "加载中...",
                    style = MaterialTheme.typography.titleLarge, // 设置为大标题
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp)) // 间距

                // 歌曲数量
                Text(
                    text = "歌曲数量: ${playlistDetail?.playlist?.trackCount ?: "未知"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
        LazyColumn {
            itemsIndexed(playlistSongs?.songs ?: emptyList()) { index, song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()  // 让卡片宽度占满
                        .padding(horizontal = 16.dp, vertical = 8.dp),  // 添加间距
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 提供阴影
                    shape = RoundedCornerShape(12.dp), // 圆角
                    onClick = { } // 点击事件
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically // 让内容垂直居中
                    ) {

                        // 序号
                        Text(text = (index + 1).toString())

                        Spacer(modifier = Modifier.width(16.dp)) // 文本和图片之间的间距

                        // 封面图片
                        AsyncImage(
                            model = song.al.picUrl,
                            contentDescription = "Album Cover",
                            modifier = Modifier
                                .size(60.dp) // 增大图片尺寸
                                .clip(RoundedCornerShape(8.dp)) // 添加圆角
                        )

                        Spacer(modifier = Modifier.width(16.dp)) // 图片和文本之间的间距

                        // 歌曲详细
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.name,
                                style = MaterialTheme.typography.titleMedium // 使用更合适的字体
                            )
                            Text(
                                text = song.ar.getOrNull(0)?.name ?: "未知歌手",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // 颜色稍浅
                            )
                            Text(
                                text = song.al.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) // 更浅的颜色
                            )
                        }
                    }
                }
            }
        }
    }
}