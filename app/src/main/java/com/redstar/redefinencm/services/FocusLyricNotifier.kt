package com.redstar.redefinencm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.R
import org.json.JSONObject
import java.util.Locale

/**
 * Helper responsible for pushing HyperOS focus island updates for the currently playing lyric.
 */
class FocusLyricNotifier(private val context: Context) : LyricCallback {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var currentTitle: String? = null
    private var currentArtist: String? = null
    private var lastTicker: String? = null

    private val isFocusSupported: Boolean by lazy {
        val manufacturer = Build.MANUFACTURER?.lowercase(Locale.ROOT) ?: return@lazy false
        if (manufacturer !in SUPPORTED_MANUFACTURERS) {
            return@lazy false
        }
        val packageManager = context.packageManager
        FOCUS_FEATURES.any { feature ->
            try {
                packageManager.hasSystemFeature(feature)
            } catch (error: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Failed to query focus feature '$feature': ${error.message}")
                }
                false
            }
        }
    }

    init {
        createChannelIfNeeded()
    }

    fun updateMetadata(title: String?, artist: String?) {
        currentTitle = title
        currentArtist = artist
    }

    override fun onLyricUpdated(lyric: String, duration: Int) {
        if (!isFocusSupported) {
            return
        }
        if (lyric.isBlank()) {
            cancelTicker()
            return
        }
        if (lastTicker == lyric) {
            return
        }
        lastTicker = lyric
        postNotification(lyric, duration)
    }

    fun onPlaybackStateChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            cancelTicker()
        }
    }

    fun cancelTicker() {
        lastTicker = null
        notificationManager.cancel(FOCUS_NOTIFICATION_ID)
    }

    private fun postNotification(lyric: String, duration: Int) {
        val extras = Bundle()
        extras.putString(MIUI_FOCUS_PARAM_KEY, buildFocusPayload(lyric, duration))

        val builder = NotificationCompat.Builder(context, FOCUS_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(currentTitle ?: context.getString(R.string.app_name))
            .setContentText(lyric)
            .setSubText(currentArtist)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setWhen(System.currentTimeMillis())

        builder.addExtras(extras)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notificationManager.notify(FOCUS_NOTIFICATION_ID, notification)
    }

    private fun buildFocusPayload(lyric: String, duration: Int): String {
        val paramV2 = JSONObject().apply {
            put("sceneId", FOCUS_SCENE_ID)
            put("ticker", lyric)
            put("tickerType", "text")
            if (!currentTitle.isNullOrBlank()) {
                put("title", currentTitle)
            }
            if (!currentArtist.isNullOrBlank()) {
                put("summary", currentArtist)
            }
            if (duration > 0) {
                put("tickerDuration", duration)
            }
        }
        val root = JSONObject().apply {
            put("param_v2", paramV2)
        }
        return root.toString()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            FOCUS_CHANNEL_ID,
            context.getString(R.string.notification_channel_focus_lyric),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_focus_lyric_description)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "FocusLyricNotifier"
        private const val FOCUS_CHANNEL_ID = "focus_lyric_channel"
        private const val FOCUS_NOTIFICATION_ID = 0xF0C5
        private const val FOCUS_SCENE_ID = "focus_lyric"
        private const val MIUI_FOCUS_PARAM_KEY = "miui.focus.param"

        private val SUPPORTED_MANUFACTURERS = setOf("xiaomi", "redmi", "poco")
        private val FOCUS_FEATURES = listOf(
            "miui.focus.device",
            "miui.focus.expand",
            "miui.focus"
        )
    }
}
