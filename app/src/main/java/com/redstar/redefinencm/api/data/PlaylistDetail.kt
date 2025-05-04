package com.redstar.redefinencm.api.data

data class playlistDetail(
    val code: Int,
    val playlist: playlistDetailPlaylist
)

data class playlistDetailPlaylist(
    val id: Long,
    val name: String,
    val coverImgUrl: String,
    val createTime: Long,
    val trackCount: Long,
    val creator: playlistDetailPlaylistCreator,
//    val tracks: List<> DO NOT USE THIS, USE /playlist/track/all
)

data class playlistDetailPlaylistCreator(
    val avatarUrl: String,
    val backgroundUrl: String,
    val userId: Long,
    val nickname: String

)