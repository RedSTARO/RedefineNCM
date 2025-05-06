package com.redstar.redefinencm.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.db.entity.UserPlaylistEntity
import com.redstar.redefinencm.util.TypeConverter

@Database(
    entities = [UserDetailEntity::class, UserPlaylistEntity::class, PlaylistDetailEntity::class, PlaylistTrackAllEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): Dao
}
