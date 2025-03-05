package com.redstar.redefinencm.api.data

data class loginQrKey(
//    {"data":{"code":200,"unikey":"----"},"code":200}
    val code: Int,
    val data: loginQrKeyData,
)

data class loginQrKeyData(
    val code: Int,
    val unikey: String,
)