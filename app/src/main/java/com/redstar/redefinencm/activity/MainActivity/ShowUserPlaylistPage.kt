package com.redstar.redefinencm.activity.MainActivity

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.dataStore
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.data.userDetail
import com.redstar.redefinencm.api.data.userPlaylist
import com.redstar.redefinencm.api.data.userPlaylistEach
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun ShowUserPlaylistPage(
    retrofit: NCMApi,
    uid: Long,
    navController: NavController,
) {
    rememberCoroutineScope()
    var userPlaylist by remember { mutableStateOf<userPlaylist?>(null) }
    var playlist by remember { mutableStateOf(emptyList<userPlaylistEach>()) }
    var userDetail by remember { mutableStateOf<userDetail?>(null) }
    var soundQuality by remember { mutableStateOf("standard") }
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        userPlaylist = retrofit.userPlaylist(uid)
        userDetail = retrofit.userDetail(uid)
        if (BuildConfig.DEBUG) {
            Log.d("showUserPlaylistPage", userDetail!!.code.toString() + userDetail!!.profile)
            Log.d(
                "showUserPlaylistPage",
                userPlaylist!!.code.toString() + userPlaylist!!.playlist
            )
        }
        playlist = userPlaylist!!.playlist
        soundQuality = context.dataStore.data
            .firstOrNull()?.get(stringPreferencesKey("onlinePlayQuality")) ?: "standard"

    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = userDetail?.profile?.backgroundUrl,
                contentDescription = "User Background",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop

            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = userDetail?.profile?.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )

                Text(
                    text = userDetail?.profile?.nickname ?: "Unknown User",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "ID: ${userDetail?.profile?.userId ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
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
                    navController
                )
            }
        }
    }

}

@Composable
fun PlaylistCard(
    userPlaylistEach: userPlaylistEach,
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
            navController.navigate("playlistDetailPage/${userPlaylistEach.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = userPlaylistEach.creator.avatarUrl,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userPlaylistEach.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = userPlaylistEach.creator.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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