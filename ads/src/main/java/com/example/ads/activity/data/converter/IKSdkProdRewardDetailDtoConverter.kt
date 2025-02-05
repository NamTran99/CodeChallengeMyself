package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdRewardDetailDto

class IKSdkProdRewardDetailDtoConverter {
    @TypeConverter
    fun fromList(value: List<IKSdkProdRewardDetailDto>?): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<List<IKSdkProdRewardDetailDto>>() {}.type
            )
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): List<IKSdkProdRewardDetailDto>? {
        return runCatching {
            val type = object : TypeToken<List<IKSdkProdRewardDetailDto>>() {}.type
            SDKDataHolder.getObjectDb<List<IKSdkProdRewardDetailDto>>(value, type)
        }.getOrNull()
    }
}