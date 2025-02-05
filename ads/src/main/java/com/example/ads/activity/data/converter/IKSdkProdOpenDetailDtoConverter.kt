package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdOpenDetailDto

class IKSdkProdOpenDetailDtoConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkProdOpenDetailDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<List<IKSdkProdOpenDetailDto>>() {}.type
            )
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkProdOpenDetailDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkProdOpenDetailDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkProdOpenDetailDto>>(value, type)
        }.getOrNull()
    }
}