package com.redstar.redefinencm.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redstar.redefinencm.RedefineNCMApplication
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

object DataStoreManager {
    private val dataStores = mutableMapOf<String, DataStore<Preferences>>()
    private val applicationContext = RedefineNCMApplication.getApplicationContext()

//    fun getUserDataStore(context: Context, userId: String): DataStore<Preferences> {
//        return dataStores.getOrPut(userId) {
//            PreferenceDataStoreFactory.create(
//                produceFile = { File(context.filesDir, "datastore/user$userId.preferences_pb") }
//            )
//        }
//    }

    private fun getAppDataStore(): DataStore<Preferences> {
        return dataStores.getOrPut("app") {
            PreferenceDataStoreFactory.create(
                produceFile = {
                    File(
                        applicationContext.filesDir,
                        "datastore/user_prefs.preferences_pb",
                    )
                },
            )
        }
    }

    suspend fun getBooleanItem(itemKey: String, defaultValue: Boolean): Boolean {
        return getAppDataStore().data
            .firstOrNull()
            ?.get(booleanPreferencesKey(itemKey)) ?: defaultValue
    }

    suspend fun setBooleanItem(itemKey: String, value: Boolean) {
        DataStoreManager.getAppDataStore().edit { preferences ->
            preferences[booleanPreferencesKey(itemKey)] = value
        }
    }

    suspend fun getStringItem(itemKey: String, defaultValue: String): String {
        return getAppDataStore().data
            .firstOrNull()
            ?.get(stringPreferencesKey(itemKey)) ?: defaultValue
    }

    suspend fun setStringItem(itemKey: String, value: String) {
        getAppDataStore().edit { preferences ->
            preferences[stringPreferencesKey(itemKey)] = value
        }
    }

    suspend fun getLongItem(itemKey: String, defaultValue: Long): Long {
        return getAppDataStore().data
            .firstOrNull()
            ?.get(longPreferencesKey(itemKey)) ?: defaultValue
    }

    suspend fun setLongItem(itemKey: String, value: Long) {
        getAppDataStore().edit { preferences ->
            preferences[longPreferencesKey(itemKey)] = value
        }
    }
}
