package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class IKSdkProdOpenDetailDto(
    @ColumnInfo(name = "screenName")
    val screenName: String,
    @ColumnInfo(name = "enable")
    var enable: Boolean? = true,
    @ColumnInfo(name = "targetNetwork")
    val targetNetwork: String? = ""
) : Parcelable
