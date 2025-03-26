package com.redstar.redefinencm.services

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.util.LyricParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class playbackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var lyricMap: LinkedHashMap<Long?, String?> = linkedMapOf() // 存储解析后的歌词
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                @OptIn(UnstableApi::class)
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val defaultResult = super.onConnect(session, controller)
                    val customCommand = SessionCommand("LYRIC_COMMAND", Bundle.EMPTY)
                    val availableCommands = defaultResult.availableSessionCommands
                        .buildUpon()
                        .add(customCommand)
                        .build()
                    return MediaSession.ConnectionResult.accept(
                        availableCommands,
                        defaultResult.availablePlayerCommands
                    )
                }

                @OptIn(UnstableApi::class)
                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    if (customCommand.customAction == "LYRIC_COMMAND") {
                        Log.d("PlaybackService", "LYRIC_COMMAND received, starting lyric sync")
                        startLyricSync() // 启动歌词同步
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
                }

            })
            .build()

        // 监听播放状态变化
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                mediaItem?.mediaId?.let { mediaId ->
                    fetchLyrics(mediaId)
                    startLyricSync()
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {
                    startLyricSync()
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
        super.onDestroy()
    }

    /**
     * 获取歌词并解析
     */
    private fun fetchLyrics(mediaId: String) {
        coroutineScope.launch {
            try {
                val response = retrofit.lyric(mediaId.toLong())
                val lyricText = response.lrc.lyric
                lyricMap = LyricParser.parse(lyricText)

                Log.d("PlaybackService", "Lyrics fetched and parsed for mediaId: $mediaId")
            } catch (e: Exception) {
                Log.e("PlaybackService", "Failed to fetch lyrics: ${e.message}")
            }
        }
    }

    /**
     * 定时匹配歌词并输出
     */
    private var lyricJob: Job? = null

    private fun startLyricSync() {
        lyricJob?.cancel() // Cancel any previous tasks to avoid duplicates
        lyricJob = coroutineScope.launch {
            while (true) {
                val isPlaying = withContext(Dispatchers.Main) { player.isPlaying }
                if (!isPlaying) break

                val currentPosition = withContext(Dispatchers.Main) { player.currentPosition }
                val currentLyric = getCurrentLyric(currentPosition)
                if (currentLyric != null) {
                    Log.d("PlaybackService", "Current Lyric: $currentLyric")
                    lyricCallback?.onLyricUpdated(currentLyric) // Send the lyric to the callback
                }
                delay(200)
            }
        }
    }



    /**
     * 获取当前时间对应的歌词
     */
    private fun getCurrentLyric(position: Long): String? {
        var lastLyric: String? = null
        for ((time, lyric) in lyricMap) {
            if (time != null && position >= time) {
                lastLyric = lyric
            } else {
                break
            }
        }
        return lastLyric
    }
}

private var lyricCallback: LyricCallback? = null

fun setLyricCallback(callback: LyricCallback) {
    lyricCallback = callback
}


interface LyricCallback {
    fun onLyricUpdated(lyric: String)
}
