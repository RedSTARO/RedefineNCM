package com.redstar.redefinencm.api.data

data class InnerVersion(
    //{"code":200,"data":{"version":"4.25.0"}}
    val code: Int,
    val data: InnerVersionData
)

data class InnerVersionData(
    val version: String
)