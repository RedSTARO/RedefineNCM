package com.redstar.redefinencm.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.redstar.redefinencm.data.api.data.CommentMusic
import com.redstar.redefinencm.data.db.dao.Dao
import com.redstar.redefinencm.data.db.entity.CommentMusicEntity
import com.redstar.redefinencm.data.db.entity.LyricEntity
import com.redstar.redefinencm.data.db.entity.PlaylistDetailEntity
import com.redstar.redefinencm.data.db.entity.PlaylistTrackAllEntity
import com.redstar.redefinencm.data.db.entity.RecommendResourceEntity
import com.redstar.redefinencm.data.db.entity.RecommendSongsEntity
import com.redstar.redefinencm.data.db.entity.UserDetailEntity
import com.redstar.redefinencm.data.db.entity.UserPlaylistEntity
import com.redstar.redefinencm.util.TypeConverter

@Database(
    entities = [UserDetailEntity::class, UserPlaylistEntity::class, PlaylistDetailEntity::class,
        PlaylistTrackAllEntity::class, RecommendResourceEntity::class, RecommendSongsEntity::class,
        CommentMusicEntity::class, LyricEntity::class],
    version = 6,
    exportSchema = false,
)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): Dao
}
