package com.redstar.redefinencm.data.api.data

data class UserPlaylist(
    val code: Int,
    val more: Boolean,
    val playlist: List<UserPlaylistEach>,
)

data class UserPlaylistEach(
    val creator: UserPlaylistEachCreator,
    val trackCount: Long,
    val playCount: Long,
    val name: String,
    val id: Long,
    val userId: Long,

    )

data class UserPlaylistEachCreator(
    val avatarUrl: String,
    val backgroundUrl: String,
    val userId: Long,
    val nickname: String,
)
