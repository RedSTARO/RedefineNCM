package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.api.data.UserPlaylistEach

@Entity(tableName = "userPlaylist")
data class UserPlaylistEntity(
    @PrimaryKey val userId: Long,
    val code: Int,
    val more: Boolean,
    val playlist: List<UserPlaylistEach>,
)
