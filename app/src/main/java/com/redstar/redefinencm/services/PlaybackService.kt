package com.redstar.redefinencm.services

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import cn.lyric.getter.api.API
import cn.lyric.getter.api.listener.LyricListener
import cn.lyric.getter.api.listener.LyricReceiver
import cn.lyric.getter.api.tools.Tools
import cn.lyric.getter.api.tools.Tools.registerLyricListener
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.R
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.util.DataStoreManager
import com.redstar.redefinencm.util.LyricParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var lyricMap: LinkedHashMap<Long?, String?> = linkedMapOf() // 存储解析后的歌词
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    val TAG = "PlaybackService"

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
        val applicationContext = RedefineNCMApplication.getApplicationContext() as Context

        // Status bar lyric
        CoroutineScope(Dispatchers.IO).launch {
            val status = (DataStoreManager.getAppDataStore().data
                .firstOrNull()
                ?.get(booleanPreferencesKey("statusBarLyric")) ?: false)

            withContext(Dispatchers.Main) {
                if (status) {
                    val receiver = LyricReceiver(object : LyricListener() {})
                    val lga by lazy { API() }

                    registerLyricListener(applicationContext, API.API_VERSION, receiver)
                    setLyricCallback(object : LyricCallback {
                        override fun onLyricUpdated(lyric: String, duration: Int) {
                            if (BuildConfig.DEBUG) {
                                Log.d("StatusBarLyric", "歌词更新： $lyric")
                            }
                            lga.sendLyric(
                                lyric,
                                extra = cn.lyric.getter.api.data.ExtraData().apply {
                                    packageName = "com.redstar.redefinencm"
                                    customIcon = true
                                    base64Icon = Tools.drawableToBase64(
                                        ContextCompat.getDrawable(
                                            applicationContext,
                                            R.drawable.ic_launcher_foreground
                                        )!!
                                    )
                                    useOwnMusicController = false
//                                    delay = duration
                                })
                        }
                    })
                    if (BuildConfig.DEBUG) {
                        Log.d("StatusBarLyric", "激活状态： ${lga.hasEnable}")
                    }

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

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            startLyricSync()
                        }

                    })
                }
            }
        }
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
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Lyrics fetched and parsed for mediaId: $mediaId")
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Failed to fetch lyrics: ${e.message}")
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
                val (currentLyric, duration) = getCurrentLyric(currentPosition)
                lyricCallback?.onLyricUpdated(currentLyric.toString(), duration.toInt())
                delay(duration)
            }
        }
    }

    /**
     * 获取当前时间对应的歌词和持续时间
     */
    private fun getCurrentLyric(position: Long): Pair<String?, Long> {
        var lastLyric: String? = null
        var lastTime: Long? = null
        var nextTime: Long? = null

        for ((time, lyric) in lyricMap) {
            if (time != null && position >= time) {
                lastLyric = lyric
                lastTime = time
            } else {
                nextTime = time
                break
            }
        }

        val duration = if (lastTime != null && nextTime != null) {
            nextTime - lastTime
        } else {
            2000L
        }

        return Pair(lastLyric, duration)
    }

    fun getLyricMap(): LinkedHashMap<Long?, String?> {
        return lyricMap
    }

}

private var lyricCallback: LyricCallback? = null

fun setLyricCallback(callback: LyricCallback) {
    lyricCallback = callback
}

interface LyricCallback {
    fun onLyricUpdated(lyric: String, duration: Int)
}
