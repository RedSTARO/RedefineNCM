package com.redstar.redefinencm.api

import com.redstar.redefinencm.api.data.loginStatus
import com.redstar.redefinencm.api.data.userAccount
import retrofit2.http.GET
import retrofit2.http.Header

interface NCMApi {
    @GET("/user/account")
    suspend fun userAccount(@Header("cookie") cookie: String): userAccount

    @GET("/login/status")
    suspend fun loginStatus(@Header("cookie") cookie: String): loginStatus
}