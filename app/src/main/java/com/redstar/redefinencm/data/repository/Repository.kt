package com.redstar.redefinencm.data.repository

import android.util.Log
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
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
        // Step 1: 从缓存获取数据
        val cachedDetail = Dao.getUserDetail(uid).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)  // 如果缓存有数据，直接返回
        }

        // Step 2: 如果缓存没有数据，去网络获取
        val networkDetail = retrofit.userDetail(uid)

        // Step 3: 把从网络获取到的数据保存到数据库
        val entity = UserDetailEntity(
            userId = networkDetail.profile.userId,
            avatarUrl = networkDetail.profile.avatarUrl,
            nickname = networkDetail.profile.nickname,
            backgroundUrl = networkDetail.profile.backgroundUrl,
            level = networkDetail.level,
            listenSongs = networkDetail.listenSongs,
            profile = networkDetail.profile
        )

        // 保存到数据库
        Dao.insertUserDetail(entity)

        // Step 4: 返回从网络获取的数据
        emit(entity)
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
        val entity = UserPlaylistEntity(
            userId = networkDetail.playlist[0].userId,
            code = networkDetail.code,
            more = networkDetail.more,
            playlist = networkDetail.playlist
        )

        // 保存到数据库
        Dao.insertUserPlaylist(entity)

        // Step 4: 返回从网络获取的数据
        emit(entity)
    }

    fun getPlaylistDetail(id: Long): Flow<PlaylistDetailEntity> = flow {
        // Step 1: 从缓存获取数据
        val cachedDetail = Dao.getPlaylistDetail(id).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "从缓存获取数据")
            emit(cachedDetail)  // 如果缓存有数据，直接返回
        }

        // Step 2: 如果缓存没有数据，去网络获取
        val networkDetail = retrofit.playlistDetail(id)

        // Step 3: 把从网络获取到的数据保存到数据库
        val entity = PlaylistDetailEntity(
            id = networkDetail.playlist.id,
            code = networkDetail.code,
            playlist = networkDetail.playlist
        )

        // 保存到数据库
        Dao.insertPlaylistDetail(entity)

        // Step 4: 返回从网络获取的数据
        emit(entity)
    }
}
