package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.data.api.data.CommentMusicComments

@Entity(tableName = "commentMusic")
data class CommentMusicEntity(
    @PrimaryKey val id: Long,
    val isMusician: Boolean,
    val userId: Long,
    val topComments: List<CommentMusicComments>,
    val moreHot: Boolean,
    val hotComments: List<CommentMusicComments>,
    val comments: List<CommentMusicComments>,
)
