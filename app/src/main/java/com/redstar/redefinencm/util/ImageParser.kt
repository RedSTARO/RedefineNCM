package com.redstar.redefinencm.util

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ImageParser {
    fun imageThemeColor(hardwareBitmap: Bitmap): Color =
        runBlocking(Dispatchers.Default) { // Using blocking coroutine due to Palette API is a blocking API
            var themeColor = Color(0xFF808080)
            Log.d(
                "imageThemeColor",
                "Hardware Bitmap: ${hardwareBitmap.width}x${hardwareBitmap.height}, Config: ${hardwareBitmap.config}"
            )

            val softwareBitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true)
            Log.d(
                "imageThemeColor",
                "Bitmap: ${softwareBitmap.width}x${softwareBitmap.height}, isRecycled: ${softwareBitmap.isRecycled}"
            )

            val palette = Palette.from(softwareBitmap).generate() // 同步生成Palette
            val muted = palette?.mutedSwatch?.rgb?.let { Color(it) }
            val vibrant = palette?.vibrantSwatch?.rgb?.let { Color(it) }
            val dominant = palette?.dominantSwatch?.rgb?.let { Color(it) }

            themeColor = (muted ?: vibrant ?: dominant ?: Color(0xFF808080)).copy(alpha = 1.0f)
            Log.d("imageThemeColor", "Theme color: $themeColor")
            themeColor // 返回提取的颜色
        }
}