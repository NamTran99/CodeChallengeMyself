package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto

class IKAdUnitDtoConverter {
    @TypeConverter
    fun fromList(value: MutableList<IKAdUnitDto>?): String {
        return kotlin.runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<MutableList<IKAdUnitDto>>() {}.type
            ) ?: ""
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): MutableList<IKAdUnitDto>? {
        return kotlin.runCatching {
            val type = object : TypeToken<MutableList<IKAdUnitDto>>() {}.type
            SDKDataHolder.getObjectDb<MutableList<IKAdUnitDto>>(value, type)
        }.getOrNull()
    }
}