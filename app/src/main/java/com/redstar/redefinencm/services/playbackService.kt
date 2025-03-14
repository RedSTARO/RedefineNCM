package com.redstar.redefinencm.services

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
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
        player = ExoPlayer.Builder(this).build()

        // TODO: test only, REMOVE ME
        CoroutineScope(Dispatchers.IO).launch {
            var id = 1379848038L
            val exampled =
                RetrofitInstance.retrofit.create(NCMApi::class.java).songDetail(listOf(id)).songs[0]
            val exampleu = RetrofitInstance.retrofit.create(NCMApi::class.java)
                .songUrlV1(listOf(id), "jymaster").data[0]
            id = 26209739L
            val ed2 =
                RetrofitInstance.retrofit.create(NCMApi::class.java).songDetail(listOf(id)).songs[0]
            val eu2 = RetrofitInstance.retrofit.create(NCMApi::class.java)
                .songUrlV1(listOf(id), "jymaster").data[0]
            withContext(Dispatchers.Main) {
                addMediaItemFromUri(
                    exampleu.url,
                    exampled.name,
                    exampled.al.name,
                    exampled.al.picUrl
                )
                addMediaItemFromUri(
                    eu2.url,
                    ed2.name,
                    ed2.al.name,
                    ed2.al.picUrl
                )
                play()
            }
        }

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
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(name)
                    .setArtist(alName)
                    .setArtworkUri(picUrl.toUri())
                    .build()
            )
            .build()
        player.addMediaItem(mediaItem)
    }

    fun play() {
        player.prepare()
        player.play()
    }
}