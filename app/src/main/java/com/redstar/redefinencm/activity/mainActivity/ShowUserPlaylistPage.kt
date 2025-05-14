package com.redstar.redefinencm.activity.mainActivity

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.data.api.data.UserPlaylistEach
import com.redstar.redefinencm.viewmodel.MainViewModel

@Composable
fun ShowUserPlaylistPage(
    navController: NavController,
    viewModel: MainViewModel,
) {
    val userDetail by viewModel.userDetail.collectAsState()
    val playlist by viewModel.userPlaylists.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
        ) {
            AsyncImage(
                model = userDetail?.profile?.backgroundUrl,
                contentDescription = "User Background",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Black layer to make icon clear
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.TopCenter)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f), // Top
                                    Color.Black.copy(alpha = 0.0f), // Buttom
                                ),
                            ),
                        )
                    },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = userDetail?.profile?.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape),
                )

                Text(
                    text = userDetail?.profile?.nickname ?: "Unknown User",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text(
                    text = "ID: ${userDetail?.profile?.userId ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f)),
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(playlist) { userPlaylistEach ->
                PlaylistCard(
                    userPlaylistEach,
                    when {
                        userPlaylistEach.name.contains("喜欢的音乐") -> "fav"
                        userPlaylistEach.name.contains("私人雷达") -> "radar"
                        else -> "no"
                    },
                    navController,
                )
            }
        }
    }
}

@Composable
fun PlaylistCard(
    userPlaylistEach: UserPlaylistEach,
    specialCard: String,
    navController: NavController,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navController.navigate("my/playlistDetailPage/${userPlaylistEach.id}")
        },
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = userPlaylistEach.creator.avatarUrl,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = userPlaylistEach.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = userPlaylistEach.creator.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
            }
            // TODO: Special cards
            if (specialCard == "fav") {
                Text(text = "心动模式todo")
            } else if (specialCard == "radar") {
                Text(text = "私人雷达todo")
            }
        }
    }
}
