package com.redstar.redefinencm.api.data

data class songUrlV1(
    val code: Int,
    val data: List<songUrlV1Data>,
)

data class songUrlV1Data(
    val id: Long,
    val url: String,
    val type: String
)