package com.redstar.redefinencm.viewmodel

import android.content.ComponentName
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
import com.redstar.redefinencm.api.data.*
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.repository.Repository
import com.redstar.redefinencm.services.PlaybackService
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainViewModel(
    private val repo: Repository
) : ViewModel() {
    private val context = RedefineNCMApplication.getApplicationContext()
    val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)

    var uid by mutableStateOf(0L)

    var mediaController = MutableStateFlow<MediaController?>(null)

    var nowPlayingMetadata = MutableStateFlow<MediaMetadata?>(null)

    var nowPayingIsPlaying = MutableStateFlow(false)

    // 用户歌单与详情
    var userPlaylists = MutableStateFlow<List<UserPlaylistEach>>(emptyList())
    var userDetail = MutableStateFlow<UserDetailEntity?>(null)

    // 歌单详情与曲目
    var playlistId = MutableStateFlow(String)
    var playlistDetail = MutableStateFlow<PlaylistDetailEntity?>(null)
    var playlistSongs = MutableStateFlow<PlaylistTrackAll?>(null)

    init {
        fetchUID()
        initMediaController()
        fetchUserData()
        fetchUserPlaylists()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            val sessionToken =
                SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            MediaController.releaseFuture(controllerFuture)
        }
    }

    private fun fetchUID() {
        runBlocking {
            val value =
                DataStoreManager.getAppDataStore().data.first()[longPreferencesKey("uid")]
            if (value != null) {
                uid = value
            }
            else {
                uid = retrofit.userAccount().account.id
                DataStoreManager.getAppDataStore().edit { preferences ->
                    preferences[longPreferencesKey("uid")] = uid
                }
            }
        }
    }

    private fun initMediaController() {
        viewModelScope.launch {
            try {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlaybackService::class.java))
                val controller = MediaController.Builder(context, sessionToken).buildAsync().await()
                mediaController.value = controller

                controller.addListener(object : Player.Listener {
                    override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                        this@MainViewModel.nowPlayingMetadata.value = metadata
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@MainViewModel.nowPayingIsPlaying.value = isPlaying
                    }
                })

                nowPlayingMetadata.value = controller.mediaMetadata
                nowPayingIsPlaying.value = controller.isPlaying
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to init MediaController: ${e.message}")
            }
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            repo.getUserDetail(uid).collect { detail ->
                userDetail.value = detail
                if (BuildConfig.DEBUG) {
                    Log.d("MainViewModel", "User Detail: ${detail.nickname}")
                }
            }
        }
    }

    fun fetchUserPlaylists(){
        viewModelScope.launch {
            repo.getUserPlaylist(uid).collect { detail ->
                userPlaylists.value = detail.playlist
            }
        }
    }

    fun fetchPlaylistDetail(songlistID: Long) {
        viewModelScope.launch {
            repo.getPlaylistDetail(songlistID).collect { detail ->
                playlistDetail.value = detail
            }
        }
    }
}
