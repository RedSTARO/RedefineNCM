package com.redstar.redefinencm.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.redstar.redefinencm.RedefineNCMApplication
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

    fun getAppDataStore(): DataStore<Preferences> {
        return dataStores.getOrPut("app") {
            PreferenceDataStoreFactory.create(
                produceFile = {
                    File(
                        applicationContext.filesDir,
                        "datastore/user_prefs.preferences_pb"
                    )
                }
            )
        }
    }
}
