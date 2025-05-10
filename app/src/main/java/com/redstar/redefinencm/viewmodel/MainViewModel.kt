package com.redstar.redefinencm.viewmodel

import android.content.ComponentName
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.api.data.SongDetailSongs
import com.redstar.redefinencm.api.data.UserPlaylistEach
import com.redstar.redefinencm.api.safeApiCall
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.RecommendResourceEntity
import com.redstar.redefinencm.data.db.entity.RecommendSongsEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.repository.Repository
import com.redstar.redefinencm.services.PlaybackService
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repo: Repository,
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
    var playlistDetail = MutableStateFlow<PlaylistDetailEntity?>(null)
    var playlistSongs = MutableStateFlow<PlaylistTrackAllEntity?>(null)

    // Recommend Page
    var recommendResource = MutableStateFlow<RecommendResourceEntity?>(null)
    var recommendSongs = MutableStateFlow<RecommendSongsEntity?>(null)

    init {
        fetchUID()
        initMediaController()
        fetchUserData()
        fetchUserPlaylists()
        fetchRecommend()
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
            } else {
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

    fun fetchUserPlaylists() {
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
        fetchPlaylistTrackAll(songlistID)
    }

    fun fetchPlaylistTrackAll(songlistID: Long) {
        viewModelScope.launch {
            repo.getPlaylistTrackAll(songlistID)
                .collect { detail ->
                    playlistSongs.value = detail
                }
        }
    }

    fun fetchRecommend() {
        viewModelScope.launch {
            repo.getRecommendResource().collect{ detail ->
                recommendResource.value = detail

            }
            repo.getRecommendSongs().collect{ detail ->
                recommendSongs.value = detail

            }
        }
    }

    fun onPlaySingleSongClick(song: SongDetailSongs) {
        if (BuildConfig.DEBUG) {
            Log.d(
                "showPlaylistDetail",
                "Selected Song ${song.name} with id ${song.id}",
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            val url = safeApiCall {
                retrofit.songUrlV1(
                    listOf(song.id),
                    DataStoreManager.getAppDataStore().data.first()[
                        stringPreferencesKey(
                            "onlinePlayQuality",
                        ),
                    ] ?: "standard",
                )
            }?.data?.get(0)
            val mediaItem = MediaItem.Builder()
                .setUri(url?.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.name)
                        .setArtist(
                            song.ar[0].name,
                        )
                        .setArtworkUri(song.al.picUrl.toUri())
                        .build(),
                )
                .setMediaId(song.id.toString())
                .build()
            withContext(Dispatchers.Main) {
                this@MainViewModel.mediaController.value?.clearMediaItems()
                this@MainViewModel.mediaController.value?.addMediaItem(mediaItem)
                this@MainViewModel.mediaController.value?.prepare()
                this@MainViewModel.mediaController.value?.play()
            }
        }
    }

    fun onPlayPlaylistClick(songlistID: Long) {
        this@MainViewModel.mediaController.value?.stop()
        this@MainViewModel.mediaController.value?.clearMediaItems()
        CoroutineScope(Dispatchers.IO).launch {
            val songDetails = safeApiCall { retrofit.playlistTrackAll(songlistID).songs }
            val songList = songDetails?.map { it.id }
            val songUrlMap = safeApiCall {
                retrofit.songUrlV1(
                    songList ?: emptyList(),
                    DataStoreManager.getAppDataStore().data.first()[stringPreferencesKey("onlinePlayQuality")]
                        ?: "standard",
                )
            }?.data?.associateBy(
                { it.id },
                { it.url },
            )

            val songInfoMap =
                songDetails?.associateBy({ it.id }, { it to songUrlMap?.get(it.id) })
            for (eachSong in songInfoMap ?: emptyMap()) {
                val mediaItem = MediaItem.Builder()
                    .setUri(eachSong.value.second)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(eachSong.value.first.name)
                            .setArtist(
                                eachSong.value.first.ar.getOrNull(0)?.name ?: "未知歌手",
                            )
                            .setArtworkUri(eachSong.value.first.al.picUrl.toUri())
                            .build(),
                    )
                    .setMediaId(eachSong.value.first.id.toString())
                    .build()
                withContext(Dispatchers.Main) {
                    this@MainViewModel.mediaController.value?.addMediaItem(mediaItem)
                }
            }
            safeApiCall { retrofit.playlistUpdatePlaycount(songlistID) }
        }
        this@MainViewModel.mediaController.value?.prepare()
        this@MainViewModel.mediaController.value?.play()
    }
}
