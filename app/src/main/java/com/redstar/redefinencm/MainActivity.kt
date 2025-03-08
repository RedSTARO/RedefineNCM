package com.redstar.redefinencm

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.api.data.userAccount
import com.redstar.redefinencm.api.data.userPlaylist
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.redstar.redefinencm.api.data.userPlaylistEach


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    println("NOW IN MAIN ACTIVITY")
                    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
                    val context = LocalContext.current
                    var uid by remember { mutableLongStateOf(0L) }
                    LaunchedEffect(Unit) {
                        uid = retrofit.userAccount().account.id
                    }
                    showUserPlaylist(retrofit, uid, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun showUserPlaylist(retrofit: NCMApi,uid: Long, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var playlist by remember { mutableStateOf(emptyList<userPlaylistEach>()) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userPlaylist = retrofit.userPlaylist(uid)
            Log.d("TEST", userPlaylist.code.toString() + userPlaylist.more + userPlaylist.playlist)
            playlist = userPlaylist.playlist
        }
    }

    println(playlist)
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(playlist) { userPlaylistEach ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = userPlaylistEach.name,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }


}

@Composable
fun showPlaylistSong(retrofit: NCMApi, modifier: Modifier = Modifier) {
    //todo
}