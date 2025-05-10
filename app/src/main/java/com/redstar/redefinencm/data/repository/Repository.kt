package com.redstar.redefinencm.data.repository

import android.util.Log
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.api.safeApiCall
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.RecommendResourceEntity
import com.redstar.redefinencm.data.db.entity.RecommendSongsEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.db.entity.UserPlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class Repository(
    private val Dao: Dao,
) {
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)

    fun getUserDetail(uid: Long): Flow<UserDetailEntity> = flow {
        val cachedDetail = Dao.getUserDetail(uid).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.userDetail(uid) }
        if (networkDetail != null) {
            val entity = UserDetailEntity(
                userId = uid,
                avatarUrl = networkDetail.profile.avatarUrl,
                nickname = networkDetail.profile.nickname,
                backgroundUrl = networkDetail.profile.backgroundUrl,
                level = networkDetail.level,
                listenSongs = networkDetail.listenSongs,
                profile = networkDetail.profile,
            )
            Dao.insertUserDetail(entity)
            emit(entity)
        }
    }

    fun getUserPlaylist(uid: Long): Flow<UserPlaylistEntity> = flow {
        val cachedDetail = Dao.getUserPlaylist(uid).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.userPlaylist(uid) }
        if (networkDetail != null) {
            val entity = UserPlaylistEntity(
                userId = uid,
                code = networkDetail.code,
                more = networkDetail.more,
                playlist = networkDetail.playlist,
            )
            Dao.insertUserPlaylist(entity)
            emit(entity)
        }
    }

    fun getPlaylistDetail(id: Long): Flow<PlaylistDetailEntity> = flow {
        val cachedDetail = Dao.getPlaylistDetail(id).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.playlistDetail(id) }
        if (networkDetail != null) {
            val entity = PlaylistDetailEntity(
                id = id,
                code = networkDetail.code,
                playlist = networkDetail.playlist,
            )
            Dao.insertPlaylistDetail(entity)
            emit(entity)
        }
    }

    fun getPlaylistTrackAll(id: Long): Flow<PlaylistTrackAllEntity> = flow {
        val cachedDetail = Dao.getPlaylistTrackAll(id).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.playlistTrackAll(id) }
        if (networkDetail != null) {
            val entity = PlaylistTrackAllEntity(
                id = id,
                code = networkDetail.code,
                songs = networkDetail.songs,
            )
            Dao.insertPlaylistTrackAll(entity)
            emit(entity)
        }
    }

    fun getRecommendSongs(): Flow<RecommendSongsEntity> = flow {
        val cachedDetail = Dao.getRecommendSongs().first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.recommendSongs() }
        if (networkDetail != null) {
            val entity = RecommendSongsEntity(
                timestamp = System.currentTimeMillis(),
                code = networkDetail.code,
                data = networkDetail.data
            )
            Dao.insertRecommendSongs(entity)
            emit(entity)
            }
        }

    fun getRecommendResource(): Flow<RecommendResourceEntity> = flow {
        val cachedDetail = Dao.getRecommendResource().first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.recommendResource() }
        if (networkDetail != null) {
            val entity = RecommendResourceEntity(
                timestamp = System.currentTimeMillis(),
                code = networkDetail.code,
                featureFirst = networkDetail.featureFirst,
                haveRcmdSongs = networkDetail.haveRcmdSongs,
                recommend = networkDetail.recommend
            )
            Dao.insertRecommendResource(entity)
            emit(entity)
        }
    }
}

