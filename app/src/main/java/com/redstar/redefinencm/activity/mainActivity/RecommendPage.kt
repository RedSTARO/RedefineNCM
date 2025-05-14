package com.redstar.redefinencm.activity.mainActivity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.data.api.data.RecommendResourceRecommend
import com.redstar.redefinencm.data.api.data.SongDetailSongs
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
                RecommendResourceCard(eachRecommend, navController)
            }
        )

        SectionWithLazyRow(
            title = "Recommend Songs",
            items = recommendSongs.value?.data?.dailySongs ?: emptyList(),
            itemContent = { eachSong ->
                RecommendSongCard(eachSong, viewModel)
            }
        )
    }
}

@Composable
fun SearchBox() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = "Search TODO", fontSize = 16.sp)
        }
    }
}

@Composable
fun RecommendResourceCard(eachRecommend: RecommendResourceRecommend, navController: NavController) {
    Card(
        onClick = { navController.navigate("recommend/playlistDetailPage/${eachRecommend.id}") },
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = eachRecommend.picUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = eachRecommend.name, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
fun RecommendSongCard(eachSong: SongDetailSongs, viewModel: MainViewModel) {
    Card(
        onClick = { viewModel.onPlaySingleSongClick(eachSong) },
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = eachSong.al.picUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = eachSong.name, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
fun <T> SectionWithLazyRow(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        if (items.isEmpty()) {
            Text(
                text = "No data available.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(8.dp)
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
