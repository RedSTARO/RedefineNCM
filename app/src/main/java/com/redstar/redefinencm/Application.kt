package com.redstar.redefinencm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RedefineNCMApplication : Application() {
    companion object {
        private var instance: RedefineNCMApplication? = null
        fun getApplicationContext(): RedefineNCMApplication {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
