package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.data.api.data.RecommendResourceRecommend

@Entity(tableName = "recommendResource")
data class RecommendResourceEntity(
    @PrimaryKey val timestamp: Long,
    val code: Int,
    val featureFirst: Boolean,
    val haveRcmdSongs: Boolean,
    val recommend: List<RecommendResourceRecommend>,
)

