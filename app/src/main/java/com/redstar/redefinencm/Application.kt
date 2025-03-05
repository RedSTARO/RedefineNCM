package com.redstar.redefinencm

import android.app.Application

class RedefineNCMApplication: Application(){
    companion object {
        private var instance: Application? = null
        fun getApplicationContext(): Application {
            return instance!!
        }
    }
        override fun onCreate() {
            super.onCreate()
            instance = this

    }
}