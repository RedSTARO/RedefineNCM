package com.redstar.redefinencm.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.redstar.redefinencm.R

/**
 * Helper responsible for driving Android Live Update lyrics.
 */
object MiuiHyperFocusLyricController {

    private const val CHANNEL_ID = "live_update_lyric"
    private const val NOTIFICATION_ID = 0x4C595243 // "LYRC"

    @Volatile
    private var lastLyric: String? = null

    fun updateLyric(
        context: Context,
        title: String?,
        artist: String?,
        currentLyric: String?,
        nextLyric: String?,
    ) {
        val lyric = currentLyric?.trim().takeUnless { it.isNullOrEmpty() } ?: return
        if (lyric == lastLyric) return

        ensureChannel(context)

        val displayTitle = title ?: context.getString(R.string.app_name)
        val detailText = buildString {
            append(lyric)
            val trimmedNext = nextLyric?.trim().orEmpty()
            if (trimmedNext.isNotEmpty()) {
                appendLine()
                append(trimmedNext)
            }
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(displayTitle)
            .setContentText(lyric)
            .setSubText(artist)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(detailText)
                    .setSummaryText(artist),
            )

        if (shouldRequestLiveUpdate(context)) {
            notificationBuilder.setRequestPromotedOngoing(true)
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build())
        lastLyric = lyric
    }

    fun clearFocus(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        lastLyric = null
    }

    fun reset() {
        lastLyric = null
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val existing = notificationManager?.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.miui_focus_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.miui_focus_channel_description)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager?.createNotificationChannel(channel)
    }

    private fun shouldRequestLiveUpdate(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        return notificationManager?.canPostPromotedNotifications() == true
    }
}
