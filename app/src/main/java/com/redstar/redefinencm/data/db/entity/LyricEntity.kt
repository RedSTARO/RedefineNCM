package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.api.data.LyricLrc

@Entity(tableName = "lyricEntity")
data class LyricEntity(
    @PrimaryKey val id: Long,
    val sgc: Boolean,
    val sfy: Boolean,
    val qfy: Boolean,
    val code: Int,
    val lrc: LyricLrc,
    val klyric: LyricLrc,
    val tlyric: LyricLrc,
)

