package com.redstar.redefinencm.api

import com.redstar.redefinencm.api.data.Dailysignin
import com.redstar.redefinencm.api.data.InnerVersion
import com.redstar.redefinencm.api.data.LoginQrCheck
import com.redstar.redefinencm.api.data.LoginQrCreate
import com.redstar.redefinencm.api.data.LoginQrKey
import com.redstar.redefinencm.api.data.LoginStatus
import com.redstar.redefinencm.api.data.Lyric
import com.redstar.redefinencm.api.data.PlaylistDetail
import com.redstar.redefinencm.api.data.PlaylistTrackAll
import com.redstar.redefinencm.api.data.SongDetail
import com.redstar.redefinencm.api.data.SongUrlV1
import com.redstar.redefinencm.api.data.UserAccount
import com.redstar.redefinencm.api.data.UserDetail
import com.redstar.redefinencm.api.data.UserPlaylist
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface NCMApi {
    @GET("/user/account")
    suspend fun userAccount(): UserAccount

    @GET("/user/detail")
    suspend fun userDetail(@Query("uid") uid: Long): UserDetail

    @POST("/login/status")
    suspend fun loginStatus(@Query("cookie") cookie: String): LoginStatus

    @POST("/login/qr/key")
    suspend fun loginQrKey(): LoginQrKey

    @POST("/login/qr/create")
    suspend fun loginQrCreate(
        @Query("key") key: String,
        @Query("qrimg") qrimg: Boolean,
    ): LoginQrCreate

    @POST("/login/qr/check")
    suspend fun loginQrCheck(@Query("key") key: String): LoginQrCheck

    @GET("/daily_signin") // This will always return 302
    suspend fun dailysignin(@Query("type") type: Int): Dailysignin

    @GET("/user/playlist")
    suspend fun userPlaylist(@Query("uid") uid: Long): UserPlaylist

    @GET("/playlist/track/all")
    suspend fun playlistTrackAll(@Query("id") id: Long): PlaylistTrackAll

    @GET("/playlist/detail")
    suspend fun playlistDetail(@Query("id") id: Long): PlaylistDetail

    @GET("/song/url/v1")
    suspend fun songUrlV1(@Query("id") id: List<Long>, @Query("level") level: String): SongUrlV1
//    standard => 标准,higher => 较高, exhigh=>极高, lossless=>无损, hires=>Hi-Res, jyeffect => 高清环绕声, sky => 沉浸环绕声, dolby => 杜比全景声, jymaster => 超清母带

    @GET("/song/detail")
    suspend fun songDetail(@Query("ids") ids: List<Long>): SongDetail

    @GET("/lyric")
    suspend fun lyric(@Query("id") id: Long): Lyric

    // @GET("/inner/version")
    @GET
    suspend fun innerVersion(@Url url: String): InnerVersion
}
