package com.redstar.redefinencm.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.db.entity.UserPlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDetail(detail: UserDetailEntity)

    @Query("SELECT * FROM userDetail WHERE userId = :uid LIMIT 1")
    fun getUserDetail(uid: Long): Flow<UserDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlaylist(detail: UserPlaylistEntity)

    @Query("SELECT * FROM userPlaylist WHERE userId = :uid LIMIT 1")
    fun getUserPlaylist(uid: Long): Flow<UserPlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistDetail(detail: PlaylistDetailEntity)

    @Query("SELECT * FROM playlistDetail WHERE id = :id LIMIT 1")
    fun getPlaylistDetail(id: Long): Flow<PlaylistDetailEntity?>
}
