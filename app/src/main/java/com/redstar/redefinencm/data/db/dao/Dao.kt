package com.redstar.redefinencm.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redstar.redefinencm.data.db.entity.CommentMusicEntity
import com.redstar.redefinencm.data.db.entity.LyricEntity
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.RecommendResourceEntity
import com.redstar.redefinencm.data.db.entity.RecommendSongsEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackAll(detail: PlaylistTrackAllEntity)

    @Query("SELECT * FROM playlistTrackAll WHERE id = :id LIMIT 1")
    fun getPlaylistTrackAll(id: Long): Flow<PlaylistTrackAllEntity?>

    @Query("SELECT * FROM recommendResource ORDER BY timestamp DESC LIMIT 1")
    fun getRecommendResource(): Flow<RecommendResourceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendResource(detail: RecommendResourceEntity)

    @Query("SELECT * FROM recommendSongs ORDER BY timestamp DESC LIMIT 1")
    fun getRecommendSongs(): Flow<RecommendSongsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendSongs(detail: RecommendSongsEntity)

    @Query("SELECT * FROM commentMusic WHERE id = :id LIMIT 1")
    fun getCommentMusic(id: Long): Flow<CommentMusicEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommentMusic(detail: CommentMusicEntity)

    @Query("SELECT * FROM lyric WHERE id = :id LIMIT 1")
    fun getLyric(id: Long): Flow<LyricEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyric(detail: LyricEntity)
}
