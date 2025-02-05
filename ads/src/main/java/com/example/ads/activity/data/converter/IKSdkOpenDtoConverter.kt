package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkOpenDto

class IKSdkOpenDtoConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkOpenDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(value, object : TypeToken<List<IKSdkOpenDto>>() {}.type)
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkOpenDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkOpenDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkOpenDto>>(value, type)
        }.getOrNull()
    }
}