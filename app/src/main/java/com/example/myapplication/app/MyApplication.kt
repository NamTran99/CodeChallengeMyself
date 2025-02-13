package com.example.myapplication.app

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication? {
            return if (Companion::instance.isInitialized) instance else null
        }
    }
    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        instance = this
        super.onCreate()
    }
}