package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import com.redstar.redefinencm.api.data.PlaylistDetailPlaylist

@Entity(tableName = "playlistDetail")
data class PlaylistDetailEntity(
    val id: String,
    val code: Int,
    val playlist: PlaylistDetailPlaylist,
)
