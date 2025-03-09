package com.redstar.redefinencm.activity.MainActivity

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
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
    Card {
        Text(playlistDetail?.playlist?.name ?: "Loading")
        Text(playlistDetail?.playlist?.trackCount?.toString()?: "Loading")
    }
    LazyColumn {
        items(playlistSongs?.songs ?: emptyList()) { song ->
            Text(text = song.name)
        }
    }

}