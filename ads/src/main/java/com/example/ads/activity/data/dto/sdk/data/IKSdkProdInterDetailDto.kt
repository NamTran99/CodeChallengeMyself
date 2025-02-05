package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class IKSdkProdInterDetailDto(
    @ColumnInfo(name = "screenName")
    val screenName: String,
    @ColumnInfo(name = "enable")
    val enable: Boolean? = true,
    @ColumnInfo(name = "timeShow")
    val timeShow: Long? = 0,
    @ColumnInfo(name = "isFirstTime")
    var isFirstTime: Boolean? = true,
    @ColumnInfo(name = "countFirstTime")
    var countFirstTime: Boolean? = true,
    @ColumnInfo(name = "showAdFrequency")
    var showAdFrequency: Int? = 1
) : Parcelable