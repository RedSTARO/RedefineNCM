package com.redstar.redefinencm.util

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

class ImageParser {
    companion object {
        private const val TAG = "ImageParser"

        /**
         * Extract the theme color from a bitmap using Palette.
         * @param hardwareBitmap Bitmap
         * @param preferStyle 0: muted, 1: vibrant, 2: dominant
         * @return Color
         */
        fun imageThemeColor(hardwareBitmap: Bitmap, preferStyle: Int = 0): Color {
            var themeColor = Color(0xFF808080)
            Log.d(
                TAG,
                "Hardware Bitmap: ${hardwareBitmap.width}x${hardwareBitmap.height}, Config: ${hardwareBitmap.config}"
            )
            val softwareBitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true)
            Log.d(
                TAG,
                "Bitmap: ${softwareBitmap.width}x${softwareBitmap.height}, isRecycled: ${softwareBitmap.isRecycled}"
            )
            val palette = Palette.from(softwareBitmap).generate() // 同步生成Palette
            val muted = palette.mutedSwatch?.rgb?.let { Color(it) }
            val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) }
            val dominant = palette.dominantSwatch?.rgb?.let { Color(it) }
            if (preferStyle == 0) {
                themeColor = muted ?: vibrant ?: dominant ?: themeColor
            } else if (preferStyle == 1) {
                themeColor = vibrant ?: muted ?: dominant ?: themeColor
            } else if (preferStyle == 2) {
                themeColor = dominant ?: vibrant ?: muted ?: themeColor
            }
            Log.d(TAG, "Theme color: $themeColor")
            return themeColor // 返回提取的颜色
        }
    }
}
