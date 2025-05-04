package com.redstar.redefinencm.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDetail(detail: UserDetailEntity)

    @Query("SELECT * FROM user_detail WHERE userId = :uid LIMIT 1")
    fun getUserDetail(uid: Long): Flow<UserDetailEntity?>
}
