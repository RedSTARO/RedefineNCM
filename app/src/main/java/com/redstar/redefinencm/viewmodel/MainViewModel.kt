package com.redstar.redefinencm.viewmodel

import android.content.ComponentName
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.api.data.playlistDetail
import com.redstar.redefinencm.api.data.playlistTrackAll
import com.redstar.redefinencm.api.data.userDetail
import com.redstar.redefinencm.api.data.userPlaylistEach
import com.redstar.redefinencm.services.playbackService
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val context = RedefineNCMApplication.getApplicationContext()
    val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)

    var uid by mutableStateOf(0L)

    var mediaController = MutableStateFlow<MediaController?>(null)

    var metadata = MutableStateFlow<MediaMetadata?>(null)

    var isPlaying = MutableStateFlow(false)

    // 用户歌单与详情
    var playlist = MutableStateFlow<List<userPlaylistEach>>(emptyList())

    var userDetail = MutableStateFlow<userDetail?>(null)

    // 歌单详情与曲目
    var playlistDetail = MutableStateFlow<playlistDetail?>(null)

    var playlistSongs = MutableStateFlow<playlistTrackAll?>(null)

    init {
        fetchUID()
        initMediaController()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            val sessionToken =
                SessionToken(context, ComponentName(context, playbackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            MediaController.releaseFuture(controllerFuture)
        }
    }

    private fun fetchUID() {
        viewModelScope.launch {
            try {
                uid = retrofit.userAccount().account.id
                if (BuildConfig.DEBUG) Log.d("MainViewModel", "UID: $uid")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch UID: ${e.message}")
            }
        }
    }

    private fun initMediaController() {
        viewModelScope.launch {
            try {
                val sessionToken =
                    SessionToken(context, ComponentName(context, playbackService::class.java))
                val controller = MediaController.Builder(context, sessionToken).buildAsync().await()
                mediaController.value = controller

                controller.addListener(object : Player.Listener {
                    override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                        this@MainViewModel.metadata.value = metadata
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@MainViewModel.isPlaying.value = isPlaying
                    }
                })

                metadata.value = controller.mediaMetadata
                isPlaying.value = controller.isPlaying
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to init MediaController: ${e.message}")
            }
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                if (uid != 0L) {
                    val detail = retrofit.userDetail(uid)
                    val playlistResponse = retrofit.userPlaylist(uid)

                    userDetail.value = detail
                    playlist.value = playlistResponse.playlist

                    if (BuildConfig.DEBUG) {
                        Log.d("MainViewModel", "UserDetail: ${detail.profile}")
                        Log.d("MainViewModel", "Playlist: ${playlistResponse.playlist}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch user data: ${e.message}")
            }
        }
    }

    fun fetchPlaylistDetail(songlistID: Long) {
        viewModelScope.launch {
            try {
                val detail = retrofit.playlistDetail(songlistID)
                val songs = retrofit.playlistTrackAll(songlistID)

                playlistDetail.value = detail
                playlistSongs.value = songs

                if (BuildConfig.DEBUG) {
                    Log.d("MainViewModel", "Playlist Detail: ${detail.playlist.name}")
                    Log.d("MainViewModel", "Playlist Songs: ${songs.songs.map { it.name }}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch playlist detail: ${e.message}")
            }
        }
    }

    fun updateDatastore(key: String, value: String) {
        viewModelScope.launch {
            DataStoreManager.getAppDataStore().edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }
}
