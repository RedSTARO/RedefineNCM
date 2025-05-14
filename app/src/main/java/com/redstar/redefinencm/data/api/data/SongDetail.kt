package com.redstar.redefinencm.data.api.data

data class SongDetail(
    val songs: List<SongDetailSongs>,
    val code: Int,
)

data class SongDetailSongs(
    val id: Long,
    val name: String,
    val ar: List<SongDetailAr>,
    val al: SongDetailAl,
    val mv: Long,
)

data class SongDetailAr(
    val id: Long,
    val name: String,
)

data class SongDetailAl(
    val id: Long,
    val name: String,
    val picUrl: String,
)
