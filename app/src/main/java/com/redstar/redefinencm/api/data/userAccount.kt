package com.redstar.redefinencm.api.data

data class userAccount(
//{"code":200,"account":
// {"id":,"userName":"","type":,"status":-10,"whitelistAuthority":0,"createTime":1714450177862,"tokenVersion":0,"ban":0,"baoyueVersion":0,"donateVersion":0,"vipType":0,"anonimousUser":true,"paidFee":false},
// "profile":null}
    val code: Int,
    val account: userAccountData,
//    val profile: Any?
)
data class userAccountData(
    val id: Long,
    val userName: String,
)