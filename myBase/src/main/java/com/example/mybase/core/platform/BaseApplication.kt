package com.example.mybase.core.platform

import android.app.Application
import android.content.Context

abstract class BaseApplication : Application() {

    companion object {
        private lateinit var instance: BaseApplication

        fun getInstance(): BaseApplication? {
            return if (Companion::instance.isInitialized) instance else null
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}