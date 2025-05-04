package com.redstar.redefinencm.api.data

data class LoginStatus(
    val data: LoginStatusData,
)

data class LoginStatusData(
    val code: Int,
    val account: LoginStatusAccount,
    val profile: LoginStatusProfile,
)

data class LoginStatusAccount(
    val id: Long,
)

data class LoginStatusProfile(
    val nickname: String,
    val avatarUrl: String,
    val backgroundUrl: String
)