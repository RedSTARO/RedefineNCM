package com.redstar.redefinencm.activity.mainActivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.redstar.redefinencm.api.data.RecommendResourceRecommend
import com.redstar.redefinencm.api.data.SongDetailSongs
import com.redstar.redefinencm.viewmodel.MainViewModel


@Composable
fun RecommendPage(
    viewModel: MainViewModel,
) {
    val recommendResource = viewModel.recommendResource.collectAsState()
    val recommendSongs = viewModel.recommendSongs.collectAsState()
    Column {
        Spacer(Modifier.padding(5.dp))
        SearchBox()

        Spacer(Modifier.padding(5.dp))
        Text("Recommend Resource")
        LazyRow {
            items(recommendResource.value?.recommend?: emptyList()) { eachRecommend ->
                RecommendResourceCard(eachRecommend)
            }
        }

        Spacer(Modifier.padding(5.dp))
        Text("Recommend Songs")
        LazyRow {
            items(recommendSongs.value?.data?.dailySongs ?: emptyList()) { eachSong ->
                RecommendSongCard(eachSong)
            }
        }
    }
}

@Composable
fun SearchBox(){
    Card {
        Text(text = "Search TODO")
    }
}

@Composable
fun RecommendResourceCard(eachRecommend: RecommendResourceRecommend){

    Spacer(Modifier.padding(5.dp))
    Card {
        Text(text = eachRecommend.name)
        AsyncImage(model = eachRecommend.picUrl, contentDescription = null, modifier = Modifier.size(150.dp))
    }
}

@Composable
fun RecommendSongCard(eachSong: SongDetailSongs){

    Spacer(Modifier.padding(5.dp))
    Card {
        Text(text = eachSong.name)
        AsyncImage(model = eachSong.al.picUrl, contentDescription = null, modifier = Modifier.size(150.dp))
    }
}



