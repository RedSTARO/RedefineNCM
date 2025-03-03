package com.redstar.redefinencm.api

import com.redstar.redefinencm.api.data.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NCMApi {
    @GET("/user/account")
    suspend fun userAccount(@Header("cookie") cookie: String): userAccount

    @GET("/login/status")
    suspend fun loginStatus(@Header("cookie") cookie: String): loginStatus

    @GET("/daily_signin") // This will always return 302
    suspend fun dailysignin(@Header("cookie") cookie: String, @Query("type") type: Int): dailysignin

    @GET("/user/playlist")
    suspend fun userPlaylist(@Header("cookie") cookie: String, @Query("uid") uid: Long): userPlaylist
}