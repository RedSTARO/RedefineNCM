package com.redstar.redefinencm.data.repository

import android.util.Log
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.db.entity.UserPlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class UserRepository(
    private val Dao: Dao,
) {
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)

    fun getUserDetail(uid: Long): Flow<UserDetailEntity> = flow {
        // Step 1: 从缓存获取数据
        val cachedDetail = Dao.getUserDetail(uid).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)  // 如果缓存有数据，直接返回
        }

        // Step 2: 如果缓存没有数据，去网络获取
        val networkDetail = retrofit.userDetail(uid)

        // Step 3: 把从网络获取到的数据保存到数据库
        val userDetailEntity = UserDetailEntity(
            userId = networkDetail.profile.userId,
            avatarUrl = networkDetail.profile.avatarUrl,
            nickname = networkDetail.profile.nickname,
            backgroundUrl = networkDetail.profile.backgroundUrl,
            level = networkDetail.level,
            listenSongs = networkDetail.listenSongs,
            profile = networkDetail.profile
        )

        // 保存到数据库
        Dao.insertUserDetail(userDetailEntity)

        // Step 4: 返回从网络获取的数据
        emit(userDetailEntity)
    }

    fun getUserPlaylist(uid: Long): Flow<UserPlaylistEntity> = flow {
        // Step 1: 从缓存获取数据
        val cachedDetail = Dao.getUserPlaylist(uid).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)  // 如果缓存有数据，直接返回
        }

        // Step 2: 如果缓存没有数据，去网络获取
        val networkDetail = retrofit.userPlaylist(uid)

        // Step 3: 把从网络获取到的数据保存到数据库
        val userPlaylistEntity = UserPlaylistEntity(
            userId = networkDetail.playlist[0].userId,
            code = networkDetail.code,
            more = networkDetail.more,
            playlist = networkDetail.playlist
        )

        // 保存到数据库
        Dao.insertUserPlaylist(userPlaylistEntity)

        // Step 4: 返回从网络获取的数据
        emit(userPlaylistEntity)
    }
}
