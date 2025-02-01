package com.example.ads.activity.data.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto

class IKAdapterDtoConverter {
    @TypeConverter
    fun fromList(value: ArrayList<IKAdapterDto>? = null): String {
        return runCatching {
            SDKDataHolder.encryptObjectDb(
                value,
                object : TypeToken<ArrayList<IKAdapterDto>>() {}.type
            )
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toList(value: String): ArrayList<IKAdapterDto>? {
        return runCatching {
            val type = object : TypeToken<ArrayList<IKAdapterDto>>() {}.type
            SDKDataHolder.getObjectDb<ArrayList<IKAdapterDto>>(value, type)
        }.getOrNull()
    }

    @TypeConverter
    fun fromObject(value: IKAdapterDto? = null): String {
        return kotlin.runCatching {
            SDKDataHolder.encryptObjectDb(value, IKAdapterDto::class.java)
        }.getOrNull() ?: ""
    }

    @TypeConverter
    fun toObject(value: String): IKAdapterDto? {
        return runCatching {
            SDKDataHolder.getObjectDb(value, IKAdapterDto::class.java)
        }.getOrNull()
    }
}