package com.redstar.redefinencm.services

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

class playbackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        // 初始化 ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // test onlY
        addMediaItemFromUri(uri = "http://m10.music.126.net/20250313200112/9faf4f6e5a64bfac59b3f07cbe4fffbd/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3?vuutv=JS601U/JkdN1WLE1Q2JtNBMGtFaNWpdeIkkh234vNStnEpweLKjw6FQtJpJHklp+FMk49GFJC4nzukzLUCoSrkvOkWjSqGivCWgIwQfBhi0=")

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

    fun addMediaItemFromUri(uri: String){
        // 设置媒体内容（示例）
        val mediaItem = MediaItem.Builder()
            .setUri(uri) // 替换为实际的音频URL
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("示例歌曲")
                    .setArtist("示例艺术家")
                    .build()
            )
            .build()
        player.setMediaItem(mediaItem)
        player.prepare() // 准备播放
        player.play()    // 开始播放，只有播放时通知才会显示
    }
}