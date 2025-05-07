package com.redstar.redefinencm.api.data

data class RecommendSongs(
    val code: Int,
    val data: RecommendSongsData,
)

data class RecommendSongsData(
    val fromCache: Boolean,
    val dailySongs: List<SongDetailSongs>,
)
