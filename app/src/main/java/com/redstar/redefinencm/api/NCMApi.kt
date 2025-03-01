package com.redstar.redefinencm.api

import com.redstar.redefinencm.api.data.userAccount
import retrofit2.http.GET

interface NCMApi {
    @GET("/user/account")
    suspend fun userAccount(): userAccount
}