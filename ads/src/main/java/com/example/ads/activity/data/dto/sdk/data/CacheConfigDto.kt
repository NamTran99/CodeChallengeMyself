package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CacheConfigDto(
    @SerializedName("forgeUpdate")
    val forgeUpdate: Boolean = false,
    @SerializedName("cacheTime")
    val cacheTime: Long
) : Parcelable
