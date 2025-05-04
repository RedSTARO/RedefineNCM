package com.redstar.redefinencm.api.data

data class loginQrCreate(
    //    {"code":200,"data":{"qrurl":"https://music.163.com/login?codekey=undefined","qrimg":""}}
    val code: Int,
    val data: loginQrCreateData,
)

data class loginQrCreateData(
    val qrurl: String,
    val qrimg: String,
)
