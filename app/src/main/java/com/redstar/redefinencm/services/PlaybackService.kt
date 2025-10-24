package com.redstar.redefinencm.services

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.Repository
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.db.DatabaseProvider
import com.redstar.redefinencm.util.LyricParser
import com.redstar.redefinencm.util.RedirectingDataSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var lyricMap: LinkedHashMap<Long?, String?> = linkedMapOf() // 存储解析后的歌词
    private val repo =
        Repository(DatabaseProvider.getDao(RedefineNCMApplication.getApplicationContext()))
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    val TAG = "PlaybackService"
    private var focusLyricNotifier: FocusLyricNotifier? = null

    object LyricBus {
        val lyricMapFlow = MutableSharedFlow<LinkedHashMap<Long?, String?>>(replay = 1)
        val lyricIndexFlow = MutableSharedFlow<Int>(replay = 1)
        val currentPosition = MutableSharedFlow<Long>(replay = 1)
        val isPlaying = MutableSharedFlow<Boolean>(replay = 1)
        val songLength = MutableSharedFlow<Long>(replay = 1)
    }

    private var lyricCallback: LyricCallback? = null
    fun setLyricCallback(callback: LyricCallback) {
        lyricCallback = callback
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val dataSourceFactory = RedirectingDataSourceFactory(
            DefaultDataSource.Factory(
                RedefineNCMApplication.getApplicationContext(),
            ),
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        player = ExoPlayer.Builder(RedefineNCMApplication.getApplicationContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        mediaSession = MediaSession.Builder(this, player).build()
        focusLyricNotifier = FocusLyricNotifier(this)
        player.currentMediaItem?.let { mediaItem ->
            focusLyricNotifier?.updateMetadata(
                mediaItem.mediaMetadata.title?.toString(),
                mediaItem.mediaMetadata.artist?.toString(),
            )
        }

        // 监听播放状态变化
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                mediaItem?.mediaId?.let { mediaId ->
                    fetchLyrics(mediaId)
                    startLyricSync()
                    focusLyricNotifier?.updateMetadata(
                        mediaItem.mediaMetadata.title?.toString(),
                        mediaItem.mediaMetadata.artist?.toString(),
                    )
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {
                    startLyricSync()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                startLyricSync()
                val songLength = player.duration
                if (songLength != C.TIME_UNSET) {
                    coroutineScope.launch {
                        LyricBus.songLength.emit(songLength)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                CoroutineScope(Dispatchers.IO).launch {
                    LyricBus.isPlaying.emit(isPlaying)
                }
                focusLyricNotifier?.onPlaybackStateChanged(isPlaying)
                if (isPlaying) {
                    startPositionSync(player)
                } else {
                    stopPositionSync()
                }
            }
        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        focusLyricNotifier?.cancelTicker()
        super.onDestroy()
    }

    /**
     * 获取歌词并解析
     */
    private fun fetchLyrics(mediaId: String) {
        coroutineScope.launch {
            repo.getLyric(mediaId.toLong()).collect {
                if (it.lrc?.lyric?.isNotEmpty() == true) {
                    val response = it
                    try {
                        val lyricText = response.lrc.lyric
                        lyricMap = LyricParser.parse(lyricText)
                        CoroutineScope(Dispatchers.IO).launch {
                            LyricBus.lyricMapFlow.emit(lyricMap)
                        }
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Lyrics fetched and parsed for mediaId: $mediaId")
                        }
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Failed to fetch lyrics: ${e.message}")
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        LyricBus.lyricMapFlow.emit(linkedMapOf(0L to "Lyric wanted"))
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "No lyrics found for mediaId: $mediaId")
                    }
                }
            }
        }
    }

    /**
     * 定时匹配歌词并输出
     */
    private var lyricJob: Job? = null

    private fun startLyricSync() {
        lyricJob?.cancel()
        lyricJob = coroutineScope.launch {
            while (true) {
                val isPlaying = withContext(Dispatchers.Main) { player.isPlaying }
                if (!isPlaying) break
                val currentPosition = withContext(Dispatchers.Main) { player.currentPosition }
                val (currentLyric, duration, index) = getCurrentLyric(currentPosition)
                val sanitizedLyric = currentLyric.orEmpty()
                lyricCallback?.onLyricUpdated(sanitizedLyric, duration.toInt())
                focusLyricNotifier?.onLyricUpdated(sanitizedLyric, duration.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    LyricBus.lyricIndexFlow.emit(index)
                    if (BuildConfig.DEBUG) {
                        Log.d("LyricSync", "歌词更新： $currentLyric")
                    }
                }
                delay(duration)
            }
        }
    }

    /**
     * 获取当前时间对应的歌词和持续时间和map的index
     */
    private fun getCurrentLyric(position: Long): Triple<String?, Long, Int> {
        var lastLyric: String? = null
        var lastTime: Long? = null
        var nextTime: Long? = null
        var index = -1
        var currentIndex = 0

        for ((time, lyric) in lyricMap) {
            if (time != null && position >= time) {
                lastLyric = lyric
                lastTime = time
                index = currentIndex
            } else {
                nextTime = time
                break
            }
            currentIndex++
        }

        val duration = if (lastTime != null && nextTime != null) {
            nextTime - lastTime
        } else {
            0L
        }

        return Triple(lastLyric, duration, index)
    }

    private var positionJob: Job? = null

    fun startPositionSync(player: ExoPlayer) {
        positionJob?.cancel()

        positionJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val currentPosition = withContext(Dispatchers.Main) { player.currentPosition }
                LyricBus.currentPosition.emit(currentPosition)
                delay(200)
            }
        }
    }

    fun stopPositionSync() {
        positionJob?.cancel()
        positionJob = null
    }
}

interface LyricCallback {
    fun onLyricUpdated(lyric: String, duration: Int)
}