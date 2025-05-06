package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.api.data.UserDetailProfile

@Entity(tableName = "userDetail")
data class UserDetailEntity(
    @PrimaryKey val userId: Long,
    val avatarUrl: String,
    val nickname: String,
    val backgroundUrl: String,
    val level: Int,
    val listenSongs: Int,
    val profile: UserDetailProfile,
)
