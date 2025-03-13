package com.redstar.redefinencm.services

import androidx.core.net.toUri
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class playbackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        // 初始化 ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // TODO: test only, REMOVE ME
        CoroutineScope(Dispatchers.IO).launch {
            val exampled =  RetrofitInstance.retrofit.create(NCMApi::class.java).songDetail(
                listOf(1379848038)).songs[0]
            val exampleu = RetrofitInstance.retrofit.create(NCMApi::class.java).songUrlV1(listOf(1379848038), "jymaster").data[0]
            println(exampleu)
            withContext(Dispatchers.Main) {
                addMediaItemFromUri(
                    exampleu.url,
                    exampled.name,
                    exampled.al.name,
                    exampled.al.picUrl
                )
            }
        }

        // 创建 MediaSession
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    fun addMediaItemFromUri(uri: String, name: String, alName: String, picUrl: String) {
        // 设置媒体内容（示例）
        val mediaItem = MediaItem.Builder()
            .setUri(uri) // 替换为实际的音频URL
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(name)
                    .setArtist(alName)
                    .setArtworkUri(picUrl.toUri())
                    .build()
            )
            .build()
        player.setMediaItem(mediaItem)
        player.prepare() // 准备播放
        player.play()    // 开始播放，只有播放时通知才会显示
    }
}