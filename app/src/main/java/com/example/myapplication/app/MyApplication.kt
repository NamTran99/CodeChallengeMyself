package com.example.myapplication.app

import android.app.Application
import android.content.Context
import com.example.myapplication.local.PrefUtil

class MyApplication: Application() {

    companion object{
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

       val pre = PrefUtil.getInstance(applicationContext)

        pre.putValue("BAC" , 5)
    }
}