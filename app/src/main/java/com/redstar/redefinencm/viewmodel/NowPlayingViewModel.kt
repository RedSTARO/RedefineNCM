package com.redstar.redefinencm.viewmodel

import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.Repository
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.data.db.DatabaseProvider
import com.redstar.redefinencm.data.db.entity.CommentMusicEntity
import com.redstar.redefinencm.services.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class NowPlayingViewModel : ViewModel() {
    private val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    private val context = RedefineNCMApplication.getApplicationContext()
    val repo = Repository(DatabaseProvider.getDao(context))
    var mediaController = MutableStateFlow<MediaController?>(null)
    var nowPlayingMetadata = MutableStateFlow<MediaMetadata?>(null)
    var nowPayingIsPlaying = MutableStateFlow(false)
//    val currentLyric = MutableStateFlow("")
    val comments = MutableStateFlow<CommentMusicEntity>(CommentMusicEntity(0, false, 0, emptyList(), false, emptyList()))
    val lyricIndex = MutableStateFlow(0)
    val lyricMap = MutableStateFlow<LinkedHashMap<Long?, String?>>(linkedMapOf(Pair(0, "Loading Lyric")))
    val playList = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentMediaIndexInList = MutableStateFlow<String?>(null)

    init {
        initMediaController()
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
                        this@NowPlayingViewModel.nowPlayingMetadata.value = metadata
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@NowPlayingViewModel.nowPayingIsPlaying.value = isPlaying
                    }
                })

                nowPlayingMetadata.value = controller.mediaMetadata
                nowPayingIsPlaying.value = controller.isPlaying
            } catch (e: Exception) {
                Log.e("NowPlayingViewModel", "Failed to init MediaController: ${e.message}")
            }
        }
    }

    fun getComments(){
        viewModelScope.launch {
            repo.getCommentMusic(mediaController.value?.currentMediaItem?.mediaId?.toLong()?: 0L).collect { detail ->
                comments.value = detail
            }
        }
    }

    fun onFavClick(){
        val mediaId = mediaController.value?.currentMediaItem?.mediaId
        CoroutineScope(Dispatchers.IO).launch {
            safeApiCall {
                RetrofitInstance.retrofit.create(NCMApi::class.java)
                    .like(mediaId?.toLong())
            }
        }
    }

    fun onPervClick() {
        mediaController.value?.seekToPrevious()
    }

    fun onPauseClick(){
        if (mediaController.value?.isPlaying?: false) mediaController.value?.pause() else mediaController.value?.play()
    }

    fun onNextClick(){
        mediaController.value?.seekToNext()
    }

    fun onSeekClick(targetId: Int){
        mediaController.value?.seekTo(targetId, 0)
    }

    fun onPlaylistClick(){
        val mediaItemCount = mediaController.value?.mediaItemCount?: 0
        for (i in 0 until mediaItemCount) {
            val mediaItem = mediaController.value?.getMediaItemAt(i)
            playList.value += mediaItem!!
        }
        val targetId = mediaController.value?.currentMediaItem?.mediaId
        currentMediaIndexInList.value = playList.value.indexOfFirst { it.mediaId == targetId }.toString()
    }

    fun onShuffleClick(status: Boolean){
        mediaController.value?.setShuffleModeEnabled(status)
    }

    fun onLyricUpdate(lyricMap_: LinkedHashMap<Long?, String?>, index_: Int){
        lyricMap.value = lyricMap_
        lyricIndex.value = index_
    }
}
