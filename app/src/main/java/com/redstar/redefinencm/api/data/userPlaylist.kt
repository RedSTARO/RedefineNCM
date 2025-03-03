package com.redstar.redefinencm.api.data

data class userPlaylist(
    val code: Int,
    val more: Boolean,
    val playlist: List<userPlaylistEach>,
)

data class userPlaylistEach(
    val creator: userPlaylistEachCreator,
    val trackCount: Int,
    val playCount: Int,
    val name: String,
    val id: Long,
    val userId: Long,

)

data class  userPlaylistEachCreator(
    val avatarUrl: String,
    val backgroundUrl: String,
    val userId: Long,
)
