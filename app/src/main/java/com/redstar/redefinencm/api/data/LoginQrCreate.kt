package com.redstar.redefinencm.api.data

data class LoginQrCreate(
    //    {"code":200,"data":{"qrurl":"https://music.163.com/login?codekey=undefined","qrimg":""}}
    val code: Int,
    val data: LoginQrCreateData,
)

data class LoginQrCreateData(
    val qrurl: String,
    val qrimg: String,
)
