package com.redstar.redefinencm.api.data

data class LoginQrKey(
    val code: Int,
    val data: LoginQrKeyData,
)

data class LoginQrKeyData(
    val code: Int,
    val unikey: String,
)
