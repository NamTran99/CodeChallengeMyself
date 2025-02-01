package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder

class IKListStringConverter {
    @TypeConverter
    fun fromList(value: List<String>?): String {
        return kotlin.runCatching {
            SDKDataHolder.encryptObjectDb(value, object : TypeToken<List<String>>() {}.type) ?: ""
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<String>? {
        return kotlin.runCatching {
            val type = object : TypeToken<List<String>>() {}.type
            SDKDataHolder.getObjectDb<List<String>>(value, type)
        }.getOrNull()
    }
}