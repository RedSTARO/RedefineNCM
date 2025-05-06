package com.redstar.redefinencm.api.data

data class PlaylistDetail(
    val code: Int,
    val playlist: PlaylistDetailPlaylist,
)

data class PlaylistDetailPlaylist(
    val id: Long,
    val name: String,
    val coverImgUrl: String,
    val createTime: Long,
    val trackCount: Long,
    val creator: PlaylistDetailPlaylistCreator,
//    val tracks: List<> DO NOT USE THIS, USE /playlist/track/all
)

data class PlaylistDetailPlaylistCreator(
    val avatarUrl: String,
    val backgroundUrl: String,
    val userId: Long,
    val nickname: String,

)
