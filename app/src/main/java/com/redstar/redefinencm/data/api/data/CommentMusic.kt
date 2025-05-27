package com.redstar.redefinencm.data.api.data

data class CommentMusic(
    val isMusician: Boolean,
    val userId: Long,
    val topComments: List<CommentMusicComments>,
    val moreHot: Boolean,
    val hotComments: List<CommentMusicComments>,
)

data class CommentMusicComments(
    val user: UserDetailProfile,
//    val beReplied: List<CommentMusicBeReplied>,
//    val showFloorComment:
    val commentId: Long,
    val content: String,
    val richContent: String,
    val time: Long,
    val timeStr: String,
    val likedCount: Int,
//    val ipLocation:
)
