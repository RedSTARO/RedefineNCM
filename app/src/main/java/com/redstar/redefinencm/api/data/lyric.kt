package com.redstar.redefinencm.api.data

data class lyric(
    val sgc: Boolean,
    val sfy: Boolean,
    val qfy: Boolean,
    val code: Int,
    val lrc: lyricLrc,
    val klyric: lyricLrc,
    val tlyric: lyricLrc,
)

data class lyricLrc(
    val version: Int,
    val lyric: String,
)
