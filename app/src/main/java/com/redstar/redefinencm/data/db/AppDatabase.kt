package com.redstar.redefinencm.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.*
import com.redstar.redefinencm.util.Converters

@Database(
    entities = [UserDetailEntity::class, UserPlaylistEntity::class, PlaylistDetailEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): Dao
}
