package com.redstar.redefinencm.data.api.data

data class SongUrlV1(
    val code: Int,
    val data: List<SongUrlV1Data>,
)

data class SongUrlV1Data(
    val id: Long,
    val url: String,
    val type: String,
)
