package com.redstar.redefinencm.viewmodel

import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.services.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class NowPlayingViewModel : ViewModel() {
    private val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    private val context = RedefineNCMApplication.getApplicationContext()
    var mediaController = MutableStateFlow<MediaController?>(null)
    var nowPlayingMetadata = MutableStateFlow<MediaMetadata?>(null)
    var nowPayingIsPlaying = MutableStateFlow(false)

    init {
        initMediaController()
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
                })

                nowPlayingMetadata.value = controller.mediaMetadata
                nowPayingIsPlaying.value = controller.isPlaying
            } catch (e: Exception) {
                Log.e("NowPlayingViewModel", "Failed to init MediaController: ${e.message}")
            }
        }
    }
}
