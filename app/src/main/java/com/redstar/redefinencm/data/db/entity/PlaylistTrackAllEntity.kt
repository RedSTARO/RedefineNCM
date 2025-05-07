package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.api.data.SongDetailSongs

@Entity(tableName = "playlistTrackAll")
data class PlaylistTrackAllEntity(
    @PrimaryKey val id: Long,
    val code: Int,
    val songs: List<SongDetailSongs>,
)
