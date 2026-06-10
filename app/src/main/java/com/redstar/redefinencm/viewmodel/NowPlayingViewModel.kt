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
        updateNowPlayingMediaIndex()
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

    /**
     * 依据当前 timeline（按播放顺序，含随机模式）重建可见播放列表、窗口顺序索引，
     * 并同步刷新当前播放项在列表中的高亮位置。
     *
     * 必须保证 [playList]、[playOrderWindowIndices] 与 [currentMediaIndexInList]
     * 始终来自同一次重建，避免随机模式下三者错位导致高亮错条目。
     */
    private fun rebuildPlaylistFromTimeline() {
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
            idx = timeline.getNextWindowIndex(idx, Player.REPEAT_MODE_OFF, shuffle)
        }

        playList.value = items
        playOrderWindowIndices.value = indices
        // 高亮位置直接由本次重建出的 indices 计算，绝不读取旧的缓存索引
        currentMediaIndexInList.value = indices.indexOf(player.currentMediaItemIndex).toString()
    }

    private fun updateNowPlayingMediaIndex() {
        // 当前播放项变化时刷新高亮；若顺序索引还未建立则先重建，保证未打开列表也能正确高亮
        val player = mediaController.value ?: return
        if (playOrderWindowIndices.value.isEmpty()) {
            rebuildPlaylistFromTimeline()
            return
        }
        currentMediaIndexInList.value =
            playOrderWindowIndices.value.indexOf(player.currentMediaItemIndex).toString()
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
                        updateNowPlayingMediaIndex()
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        // 切换随机模式会改变播放顺序，必须整体重建列表与高亮
                        shuffleStatus.value = shuffleModeEnabled
                        rebuildPlaylistFromTimeline()
                    }

                    override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                        // 队列内容/顺序变化时保持列表与高亮同步
                        rebuildPlaylistFromTimeline()
                    }

                })

                nowPlayingMetadata.value = controller.mediaMetadata
                nowPayingIsPlaying.value = controller.isPlaying
                shuffleStatus.value = controller.shuffleModeEnabled
                // 控制器就绪时队列可能已存在（如恢复播放状态），立即重建以正确高亮
                rebuildPlaylistFromTimeline()
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
        rebuildPlaylistFromTimeline()
    }


    fun onShuffleClick(status: Boolean) {
        // 仅切换模式；列表与高亮的刷新交由 onShuffleModeEnabledChanged 统一处理，
        // 确保 playList 与 currentMediaIndexInList 来自同一次重建
        mediaController.value?.setShuffleModeEnabled(status)
    }
}
