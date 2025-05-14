package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.data.api.data.PlaylistDetailPlaylist

@Entity(tableName = "playlistDetail")
data class PlaylistDetailEntity(
    @PrimaryKey val id: Long,
    val code: Int,
    val playlist: PlaylistDetailPlaylist,
)
