package com.redstar.redefinencm.viewmodel

import android.content.ComponentName
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.Repository
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.data.SongDetailSongs
import com.redstar.redefinencm.data.api.data.UserPlaylistEach
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.data.db.DatabaseProvider
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.RecommendResourceEntity
import com.redstar.redefinencm.data.db.entity.RecommendSongsEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.services.PlaybackService
import com.redstar.redefinencm.util.DataStoreManager
import com.redstar.redefinencm.util.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainViewModel(
) : ViewModel() {
    private val context = RedefineNCMApplication.getApplicationContext()
    val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)
    val repo = Repository(DatabaseProvider.getDao(RedefineNCMApplication.getApplicationContext()))

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
                DataStoreManager.getLongItem("uid", 0L)
            if (value != 0L) {
                uid = value
            } else {
                uid = retrofit.userAccount().account.id
                DataStoreManager.setLongItem("uid", uid)
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
            repo.getRecommendResource().collect { detail ->
                recommendResource.value = detail

            }
        }
        viewModelScope.launch {
            repo.getRecommendSongs().collect { detail ->
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
        viewModelScope.launch {
            val mediaItem = MediaItem.Builder()
                .setUri("redefinencm://playbackPlaceHolder?id=${song.id}")
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


    fun onPlaySingleSongInPlaylistClick(songlistID: Long, songId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val songDetails = safeApiCall { retrofit.playlistTrackAll(songlistID).songs }
            if (songDetails == null) return@launch

            val mediaItems = mutableListOf<MediaItem>()
            var targetSongIndex = 0

            songDetails.forEachIndexed { index, eachSong ->
                if (eachSong.id == songId) {
                    targetSongIndex = index
                }
                val mediaItem = MediaItem.Builder()
                    .setUri("redefinencm://playbackPlaceHolder?id=${eachSong.id}")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(eachSong.name)
                            .setArtist(eachSong.ar.first().name)
                            .setArtworkUri(eachSong.al.picUrl.toUri())
                            .build()
                    )
                    .setMediaId(eachSong.id.toString())
                    .build()
                mediaItems.add(mediaItem)
            }

            withContext(Dispatchers.Main) {
                mediaController.value?.stop()
                mediaController.value?.setMediaItems(mediaItems, targetSongIndex, 0L)
                mediaController.value?.prepare()
                mediaController.value?.play()
            }

            safeApiCall { retrofit.playlistUpdatePlaycount(songlistID) }
        }
    }


    fun onDownloadPlaylistClick(songlistID: Long) {
        viewModelScope.launch {
            val response = safeApiCall { retrofit.playlistTrackAll(songlistID) }

            val ids = response?.songs?.map { it.id }

            val inputData = workDataOf("listOfSongId" to (ids?.toLongArray() ?: emptyArray<Long>()))

            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

        }
    }

}
