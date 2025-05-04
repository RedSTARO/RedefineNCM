package com.redstar.redefinencm.data.repository

import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.data.db.dao.UserDao
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val retrofit: NCMApi
) {

    // 获取用户数据，先从缓存获取，如果缓存没有，再从网络获取
    fun getUserDetail(uid: Long): Flow<UserDetailEntity> = flow {
        // Step 1: 从缓存获取数据
        val cachedDetail = userDao.getUserDetail(uid).first()
        if (cachedDetail != null) {
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
        userDao.insertUserDetail(userDetailEntity)

        // Step 4: 返回从网络获取的数据
        emit(userDetailEntity)
    }
}
