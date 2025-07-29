package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

data class MediaItemData(
    val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val ar: String? = null,
    val artworkUri: String? = null,
)

@Entity(tableName = "playerStatus")
data class PlayerStatusEntity(
    @PrimaryKey val id: Int = 1,
    val playlist: List<MediaItemData>,
    val index: Int,
    val position: Long,
    val isPlaying: Boolean,
)
