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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.data.*
import kotlinx.coroutines.launch

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