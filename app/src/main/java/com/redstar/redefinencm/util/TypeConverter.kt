package com.redstar.redefinencm.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.redstar.redefinencm.api.data.userDetailProfile

class Converters {

    @TypeConverter
    fun fromProfile(profile: userDetailProfile): String {
        return Gson().toJson(profile)
    }

    @TypeConverter
    fun toProfile(profileString: String): userDetailProfile {
        return Gson().fromJson(profileString, userDetailProfile::class.java)
    }
}
