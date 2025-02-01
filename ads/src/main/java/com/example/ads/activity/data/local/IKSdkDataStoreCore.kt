package com.example.ads.activity.data.local

import android.content.Context
import android.content.SharedPreferences


object IKSdkDataStoreCore {

    @Volatile
    private var sharedPreferences: SharedPreferences? = null

    fun create(applicationContext: Context) {
        if (sharedPreferences == null) {
            synchronized(this) {
                if (sharedPreferences == null) {
                    kotlin.runCatching {
                        sharedPreferences =
                            applicationContext.getSharedPreferences(
                                IKSdkDataStoreConst.NAME_PREF,
                                Context.MODE_PRIVATE
                            )
                    }
                }
            }
        }
    }

    private fun get(): SharedPreferences? {
        return sharedPreferences
    }

    fun putInt(key: String, value: Int) {
        get()?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(key: String, default: Int): Int {
        return get()?.getInt(key, default) ?: default
    }

    fun putLong(key: String, value: Long) {
        get()?.edit()?.putLong(key, value)?.apply()
    }

    fun getLong(key: String, default: Long): Long {
        return get()?.getLong(key, default) ?: default
    }

    fun putString(key: String, value: String) {
        get()?.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String, default: String): String {
        return get()?.getString(key, default) ?: ""
    }

    fun putBoolean(key: String, value: Boolean) {
        get()?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return get()?.getBoolean(key, default) == true
    }

    fun remove(key: String) {
        get()?.edit()?.remove(key)?.apply()
    }

}
