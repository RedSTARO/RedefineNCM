package com.redstar.redefinencm.data

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.CommentMusicEntity
import com.redstar.redefinencm.data.db.entity.LyricEntity
import com.redstar.redefinencm.data.db.entity.PlayerStatusEntity
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.RecommendResourceEntity
import com.redstar.redefinencm.data.db.entity.RecommendSongsEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.db.entity.UserPlaylistEntity
import com.redstar.redefinencm.util.DownloadUtil
import com.redstar.redefinencm.util.SettingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class Repository(
    private val Dao: Dao,
) {
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)

    fun getUserDetail(uid: Long): Flow<UserDetailEntity> = flow {
        val cachedDetail = Dao.getUserDetail(uid).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "getUserDetail从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.userDetail(uid) }
        if (networkDetail != null && networkDetail != cachedDetail) {
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
            Log.d("UserRepository", "getUserPlaylist从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.userPlaylist(uid) }
        if (networkDetail != null && networkDetail != cachedDetail) {
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
            Log.d("UserRepository", "getPlaylistDetail从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.playlistDetail(id) }
        if (networkDetail != null && networkDetail != cachedDetail) {
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
            Log.d("UserRepository", "getPlaylistTrackAll从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.playlistTrackAll(id) }
        if (networkDetail != null && networkDetail != cachedDetail) {
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
            Log.d("UserRepository", "getRecommendSongs从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.recommendSongs() }
        if (networkDetail != null && networkDetail != cachedDetail) {
            val entity = RecommendSongsEntity(
                timestamp = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis,
                code = networkDetail.code,
                data = networkDetail.data,
            )
            Dao.insertRecommendSongs(entity)
            emit(entity)
        }
    }

    fun getRecommendResource(): Flow<RecommendResourceEntity> = flow {
        val cachedDetail = Dao.getRecommendResource().first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "getRecommendResource从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.recommendResource() }
        if (networkDetail != null && networkDetail != cachedDetail) {
            val entity = RecommendResourceEntity(
                timestamp = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis,
                code = networkDetail.code,
                featureFirst = networkDetail.featureFirst,
                haveRcmdSongs = networkDetail.haveRcmdSongs,
                recommend = networkDetail.recommend,
            )
            Dao.insertRecommendResource(entity)
            emit(entity)
        }
    }

    fun getCommentMusic(id: Long): Flow<CommentMusicEntity> = flow {
        val cachedDetail = Dao.getCommentMusic(id).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "getCommentMusic从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.commentMusic(id) }
        if (networkDetail != null && networkDetail != cachedDetail) {
            val entity = CommentMusicEntity(
                id = id,
                isMusician = networkDetail.isMusician,
                userId = networkDetail.userId,
                topComments = networkDetail.topComments,
                moreHot = networkDetail.moreHot,
                hotComments = networkDetail.hotComments,
            )
            Dao.insertCommentMusic(entity)
            emit(entity)
        }
    }

    fun getLyric(id: Long): Flow<LyricEntity> = flow {
        val cachedDetail = Dao.getLyric(id).first()
        if (cachedDetail != null) {
            Log.d("UserRepository", "getLyric从缓存获取数据")
            emit(cachedDetail)
        }

        val networkDetail = safeApiCall { retrofit.lyric(id) }
        if (networkDetail != null && networkDetail != cachedDetail) {
            val entity = LyricEntity(
                id = id,
                sgc = networkDetail.sgc,
                sfy = networkDetail.sfy,
                qfy = networkDetail.qfy,
                code = networkDetail.code,
                lrc = networkDetail.lrc,
                klyric = networkDetail.klyric,
                tlyric = networkDetail.tlyric,
            )
            Dao.insertLyric(entity)
            emit(entity)
        }
    }

    fun getSongUri(songId: Long): Uri? {
        if (DownloadUtil.fileAlreadyExistsByBaseName(songId.toString())) {
            Log.d("Repository", "Found existing file with base name $songId")
            return DownloadUtil.getExistingFileUriByBaseName(songId.toString())
        } else {
            Log.d("Repository", "Fetching uri with base name $songId")
            return runBlocking {
                safeApiCall {
                    retrofit.songUrlV1(
                        listOf(songId),
                        SettingProvider.onlinePlayQuality.lowercase()
                    ).data.first().url.toUri()
                }
            }
        }
    }

    suspend fun getPlayerStatus(): PlayerStatusEntity? {
        return Dao.getPlayerStatus()
    }

    suspend fun savePlayerStatus(status: PlayerStatusEntity) {
        Dao.savePlayerStatus(status)
    }
}
