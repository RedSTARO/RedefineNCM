package com.redstar.redefinencm.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.redstar.redefinencm.data.api.data.PlaylistDetailPlaylist
import com.redstar.redefinencm.data.api.data.RecommendResourceRecommend
import com.redstar.redefinencm.data.api.data.RecommendSongsData
import com.redstar.redefinencm.data.api.data.SongDetailSongs
import com.redstar.redefinencm.data.api.data.UserDetailProfile
import com.redstar.redefinencm.data.api.data.UserPlaylistEach

class TypeConverter {

    @TypeConverter
    fun fromProfile(profile: UserDetailProfile): String {
        return Gson().toJson(profile)
    }

    @TypeConverter
    fun toProfile(profileString: String): UserDetailProfile {
        return Gson().fromJson(profileString, UserDetailProfile::class.java)
    }

    @TypeConverter
    fun fromUserPlaylistEachList(value: List<UserPlaylistEach>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toUserPlaylistEachList(value: String): List<UserPlaylistEach> {
        val listType = object : TypeToken<List<UserPlaylistEach>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPlaylistDetailPlaylist(value: PlaylistDetailPlaylist): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPlaylistDetailPlaylist(value: String): PlaylistDetailPlaylist {
        return Gson().fromJson(value, PlaylistDetailPlaylist::class.java)
    }

    @TypeConverter
    fun fromSongDetailSongs(value: List<SongDetailSongs>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSongDetailSongs(value: String): List<SongDetailSongs> {
        val listType = object : TypeToken<List<SongDetailSongs>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromRecommendResourceRecommend(value: List<RecommendResourceRecommend>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRecommendResourceRecommend(value: String): List<RecommendResourceRecommend> {
        val listType = object : TypeToken<List<RecommendResourceRecommend>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromRecommendSongsData(value: RecommendSongsData): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRecommendSongsData(value: String): RecommendSongsData {
        return Gson().fromJson(value, RecommendSongsData::class.java)
    }
}
