package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto

class IKSdkAdDefaultConfigConverter {
    @TypeConverter
    fun fromList(value: MutableMap<String, IKAdapterDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(value, object : TypeToken<MutableMap<String, IKAdapterDto>>() {}.type)
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): MutableMap<String, IKAdapterDto>? {
        return runCatching {
            val type = object : TypeToken<MutableMap<String, IKAdapterDto>>() {}.type
            SDKDataHolder.getObjectDb<MutableMap<String, IKAdapterDto>>(value, type)
        }.getOrNull()
    }
}