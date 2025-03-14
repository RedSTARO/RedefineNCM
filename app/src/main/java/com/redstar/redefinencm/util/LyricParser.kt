package com.redstar.redefinencm.util

import android.util.Log
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Matcher
import java.util.regex.Pattern

class LyricParser {
    companion object {
        private const val TAG = "LyricParser"

        fun parse(lyric: String): LinkedHashMap<Long?, String?> {
            val lyricPair = LinkedHashMap<Long?, String?>()
            val regexWord = Regex(".*](.*)")
            val regexTime = Regex("\\[([0-9.:]*)]")
            val reader = BufferedReader(StringReader(lyric))
            var line: String? = ""
            while (reader.readLine()?.also { line = it } != null) {
                line?.let {
                    Log.d(TAG, "Origin line: $it")
                    val matchWord = regexWord.find(it)
                    val word = matchWord?.groups?.get(1)?.value
                    val matchTime = regexTime.findAll(it)
                    for (item in matchTime) {
                        val timeString = item.groups[1]?.value
                        val time = parseTimeString(timeString ?: "")
                        lyricPair[time] = word
                        Log.d(TAG, "Time:$time , Content: $word")
                    }
                }
            }
            reader.close()
            return lyricPair
        }

        private fun parseTimeString(timeString: String): Long {
            val parts = timeString.split(":")
            val minutes = parts[0].toInt()
            val seconds = parts[1].toFloat()
            return ((minutes * 60 + seconds) * 1000).toLong()
        }

        /**
         * Check if the lyric contains chinese.
         * @param lyric The lyric to check.
         * @return True if the lyric contains chinese, false otherwise.
         */
        fun isLyricContainsChinese(lyric: String): Boolean {
            val p: Pattern = Pattern.compile("[\u4e00-\u9fa5]")
            val m: Matcher = p.matcher(lyric)
            return m.find()
        }
    }
}