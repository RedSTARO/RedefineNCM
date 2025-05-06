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
                .build().also { database = it }
        }
    }

    fun getDao(context: Context): Dao {
        return getDatabase(context).getDao()
    }
}
