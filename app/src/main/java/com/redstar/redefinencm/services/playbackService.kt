package com.redstar.redefinencm.services

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class playbackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

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
                    // Accept the connection and add the custom command
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
                        lyric() // Call your custom function here
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
                }
            })
            .build()
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

    fun lyric(): Long? {
        return mediaSession?.player?.currentPosition
    }
}