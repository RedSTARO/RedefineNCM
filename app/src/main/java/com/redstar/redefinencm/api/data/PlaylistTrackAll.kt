package com.redstar.redefinencm.api.data

data class playlistTrackAll(
    val code: Int,
    val songs: List<playlistTrackAllSongs>
)

data class playlistTrackAllSongs(
    val name: String,
    val id: Long,
    val ar: List<playlistTrackAllSongsAr>,
    val al: playlistTrackAllSongsAl
)

data class playlistTrackAllSongsAr(
    val id: Long,
    val name: String,
)

data class playlistTrackAllSongsAl(
    val id: Long,
    val name: String,
    val picUrl: String,
)