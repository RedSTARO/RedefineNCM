package com.redstar.redefinencm.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.redstar.redefinencm.api.data.UserDetailProfile

class Converters {

    @TypeConverter
    fun fromProfile(profile: UserDetailProfile): String {
        return Gson().toJson(profile)
    }

    @TypeConverter
    fun toProfile(profileString: String): UserDetailProfile {
        return Gson().fromJson(profileString, UserDetailProfile::class.java)
    }
}
