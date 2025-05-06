package com.redstar.redefinencm.api.data

data class UserAccount(
// {"code":200,"account":
// {"id":,"userName":"","type":,"status":-10,"whitelistAuthority":0,"createTime":1714450177862,"tokenVersion":0,"ban":0,"baoyueVersion":0,"donateVersion":0,"vipType":0,"anonimousUser":true,"paidFee":false},
// "profile":null}
    val code: Int,
    val account: UserAccountData,
//    val profile: Any?
)

data class UserAccountData(
    val id: Long,
    val userName: String,
)
