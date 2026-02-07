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
import com.redstar.redefinencm.services.PlaybackService.LyricBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class NowPlayingViewModel : ViewModel() {
    private val context = RedefineNCMApplication.getApplicationContext()
    val repo = Repository(DatabaseProvider.getDao(context))
    var mediaController = MutableStateFlow<MediaController?>(null)
    var nowPlayingMetadata = MutableStateFlow<MediaMetadata?>(null)
    var nowPayingIsPlaying = MutableStateFlow(false)
    var shuffleStatus = MutableStateFlow(false)
    val comments = MutableStateFlow<CommentMusicEntity>(
        CommentMusicEntity(
            0,
            false,
            0,
            emptyList(),
            false,
            emptyList(),
            comments = emptyList()
        ),
    )
    val lyricIndex = MutableStateFlow(0)
    val lyricMap =
        MutableStateFlow<LinkedHashMap<Long?, String?>>(linkedMapOf(Pair(0, "Loading Lyric")))
    val playList = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentMediaIndexInList = MutableStateFlow<String?>(null)
    val isPlaying = MutableStateFlow(false)
    val currentPosition = MutableStateFlow(0L)
    val songLength = MutableStateFlow(0L)

    val playOrderWindowIndices = MutableStateFlow<List<Int>>(emptyList())


    init {
        initMediaController()
        initPlayingStatusSync()
    }

    private fun initPlayingStatusSync() {
        viewModelScope.launch {
            launch {
                LyricBus.lyricMapFlow.collect { value ->
                    lyricMap.value = value
                }
            }
            launch {
                LyricBus.lyricIndexFlow.collect { value ->
                    lyricIndex.value = value
                }
            }
            launch {
                LyricBus.currentPosition.collect { value ->
                    currentPosition.value = value
                }
            }
            launch {
                LyricBus.isPlaying.collect { value ->
                    isPlaying.value = value
                }
            }
            launch {
                LyricBus.songLength.collect { value ->
                    songLength.value = value
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
                        this@NowPlayingViewModel.nowPlayingMetadata.value = metadata
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@NowPlayingViewModel.nowPayingIsPlaying.value = isPlaying
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        // 当前播放项变化时更新 currentMediaIndexInList
                        val player = mediaController.value ?: return
                        val indices = playOrderWindowIndices.value
                        currentMediaIndexInList.value = indices.indexOf(player.currentMediaItemIndex).toString()
                    }

                })

                nowPlayingMetadata.value = controller.mediaMetadata
                nowPayingIsPlaying.value = controller.isPlaying
                shuffleStatus.value = controller.shuffleModeEnabled
            } catch (e: Exception) {
                Log.e("NowPlayingViewModel", "Failed to init MediaController: ${e.message}")
            }
        }
    }

    fun getComments() {
        Log.d("test", (mediaController.value?.mediaMetadata).toString())
        Log.d("test", (mediaController.value).toString())
        viewModelScope.launch {
            repo.getCommentMusic(mediaController.value?.currentMediaItem?.mediaId?.toLong() ?: 0L)
                .collect { detail ->
                    comments.value = detail
                }
        }
    }

    fun onFavClick() {
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

    fun onPauseClick() {
        if (mediaController.value?.isPlaying
                ?: false
        ) {
            mediaController.value?.pause()
        } else {
            mediaController.value?.play()
        }
    }

    fun onNextClick() {
        mediaController.value?.seekToNext()
    }

    fun onSeekClick(position: Int) {
        val player = mediaController.value ?: return
        val windowIndex = playOrderWindowIndices.value.getOrNull(position) ?: return
        player.seekTo(windowIndex, 0L)
    }


    fun onPositionSeekClick(newPosition: Long) {
        mediaController.value?.seekTo(newPosition)
    }

    fun onPlaylistClick() {
        val player = mediaController.value ?: return
        val timeline = player.currentTimeline

        if (timeline.isEmpty) {
            playList.value = emptyList()
            playOrderWindowIndices.value = emptyList()
            currentMediaIndexInList.value = "-1"
            return
        }

        val shuffle = player.shuffleModeEnabled

        val items = mutableListOf<MediaItem>()
        val indices = mutableListOf<Int>()

        var idx = timeline.getFirstWindowIndex(shuffle)
        while (idx != androidx.media3.common.C.INDEX_UNSET) {
            items += player.getMediaItemAt(idx)
            indices += idx

            idx = timeline.getNextWindowIndex(
                idx,
                Player.REPEAT_MODE_OFF,
                shuffle
            )
        }

        playList.value = items
        playOrderWindowIndices.value = indices

        val curWindowIdx = player.currentMediaItemIndex
        currentMediaIndexInList.value = indices.indexOf(curWindowIdx).toString()
    }



    fun onShuffleClick(status: Boolean) {
        shuffleStatus.value = status
        mediaController.value?.setShuffleModeEnabled(status)
    }
}
