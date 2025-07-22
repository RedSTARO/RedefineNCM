package com.redstar.redefinencm.activity.mainActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.viewmodel.MainViewModel

@Composable
fun RecommendPage(
    navController: NavController,
    viewModel: MainViewModel,
) {
    val recommendResource = viewModel.recommendResource.collectAsState()
    val recommendSongs = viewModel.recommendSongs.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        SearchBox()

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

@Composable
fun SearchBox() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = "Search TODO", fontSize = 16.sp)
        }
    }
}

@Composable
fun RecommendSquareCard(picUrl: String, text: String, onClick: () -> Unit) {
    Card(
        onClick = { onClick() },
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        elevation = CardDefaults.cardElevation(4.dp),
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        if (items.isEmpty()) {
            Text(
                text = "No data available.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
