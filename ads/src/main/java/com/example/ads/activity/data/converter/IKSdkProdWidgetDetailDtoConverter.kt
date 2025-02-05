package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto

class IKSdkProdWidgetDetailDtoConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkProdWidgetDetailDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<List<IKSdkProdWidgetDetailDto>>() {}.type
            )
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkProdWidgetDetailDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkProdWidgetDetailDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkProdWidgetDetailDto>>(value, type)
        }.getOrNull()
    }
}