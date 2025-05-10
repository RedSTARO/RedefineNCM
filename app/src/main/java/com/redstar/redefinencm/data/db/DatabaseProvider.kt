// DatabaseProvider.kt
package com.redstar.redefinencm.data.db

import android.content.Context
import androidx.room.Room
import com.redstar.redefinencm.data.db.dao.Dao

object DatabaseProvider {

    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "redefinencm.db",
            )
                .fallbackToDestructiveMigration(true) // 强制重建数据库
                .build().also { database = it }
        }
    }

    fun getDao(context: Context): Dao {
        return getDatabase(context).getDao()
    }
}
