package com.redstar.redefinencm.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hyperfocus.api.FocusApi
import com.hyperfocus.api.IslandApi
import com.redstar.redefinencm.R
import org.json.JSONObject

/**
 * Helper responsible for driving Xiaomi HyperOS 3 focus island lyrics.
 */
object MiuiHyperFocusLyricController {

    private const val CHANNEL_ID = "miui_focus_lyric"
    private const val NOTIFICATION_ID = 0x4C595243 // "LYRC"
    private const val MIUI_EFFECT_SRC_KEY = "miui.effect.src"

    private val isHyperOs3Device: Boolean by lazy { detectHyperOs3() }

    @Volatile
    private var lastLyric: String? = null

    fun updateLyric(
        context: Context,
        title: String?,
        artist: String?,
        currentLyric: String?,
        nextLyric: String?,
    ) {
        if (!isHyperOs3Device) return
        val lyric = currentLyric?.trim().takeUnless { it.isNullOrEmpty() } ?: return
        if (lyric == lastLyric) return

        ensureChannel(context)

        val displayTitle = title ?: context.getString(R.string.app_name)
        val tickerText = buildString {
            append(displayTitle)
            append(" · ")
            append(lyric)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(displayTitle)
            .setContentText(lyric)
            .setTicker(tickerText)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)

        val baseInfo = FocusApi.baseinfo(
            title = displayTitle,
            content = lyric,
            subTitle = artist,
            colorTitle = "#FFFFFFFF",
            colorContent = "#E6FFFFFF",
            colorsubTitle = "#B3FFFFFF",
        )

        val islandTemplate = createIslandTemplate(lyric, artist, nextLyric)

        val focusExtras = FocusApi.sendFocus(
            title = displayTitle,
            content = lyric,
            baseInfo = baseInfo,
            ticker = tickerText,
            picticker = Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            island = islandTemplate,
            enableFloat = true,
            islandFirstFloat = true,
            updatable = true,
            showSmallIcon = false,
            hideDeco = true,
            isShowNotification = true,
        )

        val miuiExtras = Bundle().apply {
            putString(MIUI_EFFECT_SRC_KEY, "true")
            putAll(focusExtras)
        }

        notificationBuilder.addExtras(miuiExtras)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build())
        lastLyric = lyric
    }

    fun clearFocus(context: Context) {
        if (!isHyperOs3Device) return
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        lastLyric = null
    }

    fun reset() {
        lastLyric = null
    }

    private fun createIslandTemplate(
        lyric: String,
        artist: String?,
        nextLyric: String?,
    ): JSONObject {
        val lyricLine = IslandApi.TextInfo(
            title = lyric,
            frontTitle = artist,
            content = nextLyric?.takeIf { it.isNotBlank() },
            showHighlightColor = false,
            turnAnim = true,
        )

        val nextLine = nextLyric?.takeIf { it.isNotBlank() }?.let {
            IslandApi.sameWidthDigitInfo(
                content = it,
                showHighlightColor = false,
                turnAnim = true,
            )
        }

        val bigIslandArea = IslandApi.bigIslandArea(
            textInfo = lyricLine,
            sameWidthDigitInfo = nextLine,
        )

        val smallIslandArea = IslandApi.SmallIslandArea(
            picInfo = IslandApi.picInfo(
                autoplay = true,
                loop = true,
                effectColor = "#66FFFFFF",
                pic = "musicWave",
                type = 2,
            ),
        )

        return IslandApi.IslandTemplate(
            business = "music",
            bigIslandArea = bigIslandArea,
            smallIslandArea = smallIslandArea,
            highlightColor = "#80FFFFFF",
            islandOrder = true,
            islandPriority = 2,
            islandProperty = 2,
            islandTimeout = 280,
            needCloseAnimation = true,
        )
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

    @SuppressLint("PrivateApi")
    private fun detectHyperOs3(): Boolean {
        if (!Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            return false
        }
        val osName = getSystemProperty("ro.mi.os.version.name")
        if (!osName.isNullOrEmpty()) {
            return osName.startsWith("OS3", ignoreCase = true)
        }
        return false
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String): String? {
        return runCatching {
            val clazz = Class.forName("android.os.SystemProperties")
            val getter = clazz.getMethod("get", String::class.java)
            (getter.invoke(null, key) as? String)?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

