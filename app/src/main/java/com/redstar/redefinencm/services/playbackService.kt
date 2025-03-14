package com.redstar.redefinencm.services

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class playbackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        // TODO: test only, REMOVE ME
//        CoroutineScope(Dispatchers.IO).launch {
//            var id = 1379848038L
//            val exampled =
//                RetrofitInstance.retrofit.create(NCMApi::class.java).songDetail(listOf(id)).songs[0]
//            val exampleu = RetrofitInstance.retrofit.create(NCMApi::class.java)
//                .songUrlV1(listOf(id), "jymaster").data[0]
//            id = 26209739L
//            val ed2 =
//                RetrofitInstance.retrofit.create(NCMApi::class.java).songDetail(listOf(id)).songs[0]
//            val eu2 = RetrofitInstance.retrofit.create(NCMApi::class.java)
//                .songUrlV1(listOf(id), "jymaster").data[0]
//            withContext(Dispatchers.Main) {
//                var mediaItem = MediaItem.Builder()
//                    .setUri(exampleu.url)
//                    .setMediaMetadata(
//                        MediaMetadata.Builder()
//                            .setTitle(exampled.name)
//                            .setArtist(exampled.al.name)
//                            .setArtworkUri(exampled.al.picUrl.toUri())
//                            .build()
//                    )
//                    .build()
//                player.addMediaItem(mediaItem)
//                mediaItem = MediaItem.Builder()
//                    .setUri(eu2.url)
//                    .setMediaMetadata(
//                        MediaMetadata.Builder()
//                            .setTitle(ed2.name)
//                            .setArtist(ed2.al.name)
//                            .setArtworkUri(ed2.al.picUrl.toUri())
//                            .build()
//                    )
//                    .build()
//                player.addMediaItem(mediaItem)
//                player.prepare()
//                player.play()
//            }
//        }

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
}