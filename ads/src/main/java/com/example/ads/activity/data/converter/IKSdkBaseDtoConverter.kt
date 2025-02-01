package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto

class IKSdkBaseDtoConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkBaseDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(value, object : TypeToken<List<IKSdkBaseDto>>() {}.type)
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkBaseDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkBaseDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkBaseDto>>(value, type)
        }.getOrNull()
    }
}