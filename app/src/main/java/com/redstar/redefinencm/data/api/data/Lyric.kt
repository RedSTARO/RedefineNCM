package com.redstar.redefinencm.data.api.data

data class Lyric(
    val sgc: Boolean,
    val sfy: Boolean,
    val qfy: Boolean,
    val code: Int,
    val lrc: LyricLrc,
    val klyric: LyricLrc,
    val tlyric: LyricLrc,
)

data class LyricLrc(
    val version: Int,
    val lyric: String,
)
