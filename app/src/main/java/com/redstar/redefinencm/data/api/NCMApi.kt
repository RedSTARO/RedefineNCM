package com.redstar.redefinencm.data.api

import com.redstar.redefinencm.data.api.data.Dailysignin
import com.redstar.redefinencm.data.api.data.InnerVersion
import com.redstar.redefinencm.data.api.data.Like
import com.redstar.redefinencm.data.api.data.LikeList
import com.redstar.redefinencm.data.api.data.LoginQrCheck
import com.redstar.redefinencm.data.api.data.LoginQrCreate
import com.redstar.redefinencm.data.api.data.LoginQrKey
import com.redstar.redefinencm.data.api.data.LoginStatus
import com.redstar.redefinencm.data.api.data.Lyric
import com.redstar.redefinencm.data.api.data.PlaylistDetail
import com.redstar.redefinencm.data.api.data.PlaylistTrackAll
import com.redstar.redefinencm.data.api.data.PlaylistUpdatePlayCount
import com.redstar.redefinencm.data.api.data.RecommendResource
import com.redstar.redefinencm.data.api.data.RecommendSongs
import com.redstar.redefinencm.data.api.data.SongDetail
import com.redstar.redefinencm.data.api.data.SongUrlV1
import com.redstar.redefinencm.data.api.data.UserAccount
import com.redstar.redefinencm.data.api.data.UserDetail
import com.redstar.redefinencm.data.api.data.UserPlaylist
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

    @GET("/playlist/update/playcount")
    suspend fun playlistUpdatePlaycount(@Query("id") id: Long): PlaylistUpdatePlayCount

    @GET("/song/url/v1")
    suspend fun songUrlV1(@Query("id") id: List<Long>, @Query("level") level: String): SongUrlV1
//    standard => 标准,higher => 较高, exhigh=>极高, lossless=>无损, hires=>Hi-Res, jyeffect => 高清环绕声, sky => 沉浸环绕声, dolby => 杜比全景声, jymaster => 超清母带

    @GET("/song/detail")
    suspend fun songDetail(@Query("ids") ids: List<Long>): SongDetail

    @GET("/recommend/songs")
    suspend fun recommendSongs(): RecommendSongs

    @GET("/recommend/resource")
    suspend fun recommendResource(): RecommendResource

    @GET("/lyric")
    suspend fun lyric(@Query("id") id: Long): Lyric

    @POST("/like")
    suspend fun like(@Query("id") id: Long?): Like

    @POST("/likelist")
    suspend fun likelist(@Query("uid") uid: Long): LikeList

    // @GET("/inner/version")
    @GET
    suspend fun innerVersion(@Url url: String): InnerVersion
}
