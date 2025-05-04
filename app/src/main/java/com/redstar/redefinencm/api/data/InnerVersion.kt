package com.redstar.redefinencm.api.data

data class innerVersion(
    //{"code":200,"data":{"version":"4.25.0"}}
    val code: Int,
    val data: innerVersionData
)

data class innerVersionData(
    val version: String
)