package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.data.api.data.RecommendSongsData

@Entity(tableName = "recommendSongs")
data class RecommendSongsEntity(
    @PrimaryKey val timestamp: Long,
    val code: Int,
    val data: RecommendSongsData,
)


