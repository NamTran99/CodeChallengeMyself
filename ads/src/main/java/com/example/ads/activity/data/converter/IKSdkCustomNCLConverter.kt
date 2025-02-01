package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkCustomNCLDetailDto

class IKSdkCustomNCLConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkCustomNCLDetailDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<List<IKSdkCustomNCLDetailDto>>() {}.type
            )
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkCustomNCLDetailDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkCustomNCLDetailDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkCustomNCLDetailDto>>(value, type)
        }.getOrNull()
    }
}