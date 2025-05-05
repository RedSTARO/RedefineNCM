package com.redstar.redefinencm.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.redstar.redefinencm.api.data.*

class Converters {

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
}
