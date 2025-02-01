package com.example.myapplication.app

import android.app.Application
import android.content.Context
import com.example.myapplication.local.PrefUtil
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        super.onCreate()
        appContext = applicationContext
    }
}