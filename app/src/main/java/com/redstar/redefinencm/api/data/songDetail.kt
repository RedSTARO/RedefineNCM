package com.redstar.redefinencm.api.data

data class songDetail(
    val songs: List<songDetailSongs>,
    val code: Int
)

data class songDetailSongs(
    val id: Long,
    val name: String,
    val ar: List<songDetailAr>,
    val al: songDetailAl,
    val mv: Long
)

data class songDetailAr(
    val id: Long,
    val name: String
)

data class songDetailAl(
    val id: Long,
    val name: String,
    val picUrl: String
)