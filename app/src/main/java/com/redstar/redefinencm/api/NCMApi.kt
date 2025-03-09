package com.redstar.redefinencm.api

import com.redstar.redefinencm.api.data.*
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NCMApi {
    @GET("/user/account")
    suspend fun userAccount(): userAccount

    @POST("/login/status")
    suspend fun loginStatus(@Query("cookie") cookie: String): loginStatus

    @POST("/login/qr/key")
    suspend fun loginQrKey(): loginQrKey

    @POST("/login/qr/create")
    suspend fun loginQrCreate(@Query("key") key: String, @Query("qrimg") qrimg: Boolean): loginQrCreate

    @POST("/login/qr/check")
    suspend fun loginQrCheck(@Query("key") key: String): loginQrCheck

    @GET("/daily_signin") // This will always return 302
    suspend fun dailysignin(@Query("type") type: Int): dailysignin

    @GET("/user/playlist")
    suspend fun userPlaylist(@Query("uid") uid: Long): userPlaylist

    @GET("/playlist/track/all")
    suspend fun playlistTrackAll(@Query("id") id: Long): playlistTrackAll

    @GET("/playlist/detail")
    suspend fun playlistDetail(@Query("id") id: Long): playlistDetail

    @GET("song/url/v1")
    suspend fun songUrlV1(@Query("id") id: List<Long> ,@Query("level") level: String): songUrlV1
//    standard => 标准,higher => 较高, exhigh=>极高, lossless=>无损, hires=>Hi-Res, jyeffect => 高清环绕声, sky => 沉浸环绕声, dolby => 杜比全景声, jymaster => 超清母带
}