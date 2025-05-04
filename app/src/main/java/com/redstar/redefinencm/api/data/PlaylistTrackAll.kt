package com.redstar.redefinencm.api.data

data class PlaylistTrackAll(
    val code: Int,
    val songs: List<PlaylistTrackAllSongs>
)

data class PlaylistTrackAllSongs(
    val name: String,
    val id: Long,
    val ar: List<PlaylistTrackAllSongsAr>,
    val al: PlaylistTrackAllSongsAl
)

data class PlaylistTrackAllSongsAr(
    val id: Long,
    val name: String,
)

data class PlaylistTrackAllSongsAl(
    val id: Long,
    val name: String,
    val picUrl: String,
)