package com.redstar.redefinencm.activity.MainActivity

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.data.playlistDetail
import androidx.navigation.NavController

@Composable
fun showPlaylistDetailPage(songlistID: Long, retrofit: NCMApi,navController: NavController, modifier: Modifier = Modifier) {
    var playlistDetail by remember { mutableStateOf<playlistDetail?>(null) }
    LaunchedEffect(Unit) {
        playlistDetail = retrofit.playlistDetail(songlistID)
    }
    Card {
        Text(playlistDetail?.playlist?.name ?: "Loading")
    }
}