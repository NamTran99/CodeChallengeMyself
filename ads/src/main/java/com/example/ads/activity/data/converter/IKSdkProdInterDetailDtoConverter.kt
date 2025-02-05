package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDetailDto

class IKSdkProdInterDetailDtoConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkProdInterDetailDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<List<IKSdkProdInterDetailDto>>() {}.type
            )
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkProdInterDetailDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkProdInterDetailDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkProdInterDetailDto>>(value, type)
        }.getOrNull()
    }
}