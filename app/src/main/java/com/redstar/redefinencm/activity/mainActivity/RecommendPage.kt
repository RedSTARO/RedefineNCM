package com.redstar.redefinencm.activity.mainActivity

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.redstar.redefinencm.viewmodel.MainViewModel

@Composable
fun RecommendPage(
    navController: NavController,
    viewModel: MainViewModel,
    sharedTransitionScope: SharedTransitionScope,
) {
    val recommendResource = viewModel.recommendResource.collectAsState()
    val recommendSongs = viewModel.recommendSongs.collectAsState()
    var showSearch by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = !showSearch) {
            val animatedVisibilityScope = this
            Column(modifier = Modifier.padding(16.dp)) {
                SearchBox(
                    onClick = { showSearch = true },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )

                SectionWithLazyRow(
                    title = "Recommend Resources",
                    items = recommendResource.value?.recommend ?: emptyList(),
                    itemContent = { eachRecommend ->

                        RecommendSquareCard(
                            eachRecommend.picUrl,
                            eachRecommend.name,
                            { navController.navigate("recommend/playlistDetailPage/${eachRecommend.id}") },
                        )
                    },
                )

                SectionWithLazyRow(
                    title = "Recommend Songs",
                    items = recommendSongs.value?.data?.dailySongs ?: emptyList(),
                    itemContent = { eachSong ->
                        RecommendSquareCard(
                            eachSong.al.picUrl,
                            eachSong.name,
                            { viewModel.onPlaySingleSongClick(eachSong) },
                        )
                    },
                )
            }
        }

        AnimatedVisibility(visible = showSearch) {
            val animatedVisibilityScope = this
            SearchDemoPage(
                onBack = { showSearch = false },
                viewModel = viewModel,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchBox(
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    with(sharedTransitionScope) {
        // Expressive search field: fully-rounded pill, tonal container, leading icon.
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .sharedBounds(
                    rememberSharedContentState(SharedKeys.search()),
                    animatedVisibilityScope,
                )
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "Search songs, playlists…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun RecommendSquareCard(picUrl: String, text: String, onClick: () -> Unit) {
    Card(
        onClick = { onClick() },
        modifier = Modifier
            .padding(8.dp)
            .size(160.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = picUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            if (text != "私人雷达") {
                // Black Background for text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 200f,
                            ),
                        ),
                )

                Text(
                    text = text,
                    fontSize = 17.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .fillMaxWidth()
                        .basicMarquee(), // Scroll display
                )
            } else {
                Text(
                    text = "", // 私人雷达 already have text on picture
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
fun <T> SectionWithLazyRow(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        if (items.isEmpty()) {
            Text(
                text = "No data available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
        } else {
            LazyRow {
                items(items) { item ->
                    itemContent(item)
                }
            }
        }
    }
}
