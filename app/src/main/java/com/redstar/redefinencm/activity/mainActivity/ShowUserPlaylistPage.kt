package com.redstar.redefinencm.activity.mainActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.redstar.redefinencm.data.api.data.UserPlaylistEach
import com.redstar.redefinencm.ui.component.ExpressiveSectionTitle
import com.redstar.redefinencm.ui.component.connectedListItemShape
import com.redstar.redefinencm.viewmodel.MainViewModel
import com.skydoves.cloudy.cloudy

@Composable
fun ShowUserPlaylistPage(
    navController: NavController,
    viewModel: MainViewModel,
) {
    val userDetail by viewModel.userDetail.collectAsState()
    val playlist by viewModel.userPlaylists.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        item {
            UserPlaylistHero(
                backgroundUrl = userDetail?.profile?.backgroundUrl,
                avatarUrl = userDetail?.profile?.avatarUrl,
                nickname = userDetail?.profile?.nickname ?: "Unknown User",
                userId = userDetail?.profile?.userId?.toString() ?: "N/A",
            )
        }
        item {
            ExpressiveSectionTitle(
                text = "My Playlists",
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 12.dp),
            )
        }
        itemsIndexed(playlist) { index, userPlaylistEach ->
            PlaylistCard(
                userPlaylistEach = userPlaylistEach,
                specialCard = when {
                    userPlaylistEach.name.contains("喜欢的音乐") -> "fav"
                    userPlaylistEach.name.contains("私人雷达") -> "radar"
                    else -> "no"
                },
                index = index,
                count = playlist.size,
                navController = navController,
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun UserPlaylistHero(
    backgroundUrl: String?,
    avatarUrl: String?,
    nickname: String,
    userId: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            ),
    ) {
        AsyncImage(
            model = backgroundUrl,
            contentDescription = "User Background",
            modifier = Modifier
                .fillMaxSize()
                .cloudy(radius = 30)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent,
                            ),
                        ),
                    )
                },
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = nickname,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(),
            )
            Text(
                text = "ID: $userId",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
fun PlaylistCard(
    userPlaylistEach: UserPlaylistEach,
    specialCard: String,
    index: Int,
    count: Int,
    navController: NavController,
) {
    Surface(
        onClick = {
            navController.navigate("my/playlistDetailPage/${userPlaylistEach.id}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.5.dp),
        shape = connectedListItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = userPlaylistEach.creator.avatarUrl,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.large),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userPlaylistEach.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
                Text(
                    text = userPlaylistEach.creator.nickname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${userPlaylistEach.trackCount} songs · ${compactCount(userPlaylistEach.playCount)} plays",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (specialCard != "no") {
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = CircleShape,
                    color = if (specialCard == "fav") MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (specialCard == "fav") MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(
                        imageVector = if (specialCard == "fav") Icons.Filled.Favorite else Icons.Filled.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp),
                    )
                }
            }
        }
    }
}

private fun compactCount(value: Long): String {
    return when {
        value >= 100_000_000L -> "${value / 100_000_000L}亿"
        value >= 10_000L -> "${value / 10_000L}万"
        else -> value.toString()
    }
}
