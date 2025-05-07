package com.redstar.redefinencm.api.data

data class RecommendResource(
    val code: Int,
    val featureFirst: Boolean,
    val haveRcmdSongs: Boolean,
    val recommend: List<RecommendResourceRecommend>,
)

data class RecommendResourceRecommend(
    val id: Long,
    val type: Int,
    val name: String,
    val copywriter: String,
    val picUrl: String,
    val playCount: Long,
    val createTime: Long,
    val creator: RecommendResourceRecommendCreator,
    val trackCount: Int,
    val userId: Long,
    val alg: String,
)

data class RecommendResourceRecommendCreator(
    val avatarImgIdStr: String,
    val backgroundImgIdStr: String,
    val city: Long,
    val vipType: Int,
    val province: Long,
    val birthday: Long,
    val accountStatus: Int,
    val avatarUrl: String,
    val authStatus: Int,
    val userType: Int,
    val nickname: String,
    val gender: Int,
    val backgroundUrl: String,
    val avatarImgId: Long,
    val backgroundImgId: Long,
    val detailDescription: String,
    val defaultAvatar: Boolean,
    val expertTags: Any,
    val djStatus: Int,
    val followed: Boolean,
    val mutual: Boolean,
    val remarkName: Any,
    val description: String,
    val userId: Long,
    val signature: String,
    val authority: Long,
)
