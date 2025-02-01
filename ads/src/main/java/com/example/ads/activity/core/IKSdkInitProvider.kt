package com.example.ads.activity.core

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
//import com.ikame.android.datasourceadapter.IKDataSourceHelper

class IKSdkInitProvider : Initializer<Unit> {
    override fun create(context: Context) {
//        val dataSourceHelper = kotlin.runCatching {
//            Class.forName(IKDataSourceHelper::class.java.name)
//            true
//        }.getOrNull() ?: false
//        if (!dataSourceHelper)
//            throw RuntimeException("iKame Data Source not found")
//        IKSdkApplicationProvider.init(context.applicationContext as Application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}