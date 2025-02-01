package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class IKSdkCustomNCLDetailDto(
    @ColumnInfo(name = "screenName")
    var screenName: String?,
    @ColumnInfo(name = "adFormat")
    var adFormat: String?,
    @ColumnInfo(name = "adNetwork")
    var adNetwork: String?,
    @ColumnInfo(name = "timeOutClose")
    var timeOutClose: Long? = 0L,
    @ColumnInfo(name = "timeOutLoad")
    var timeOutLoad: Long? = 0L,
    @ColumnInfo(name = "enableLoad")
    var enableLoad: Boolean? = true,
    @ColumnInfo(name = "enable")
    var enable: Boolean? = true
) : Parcelable
