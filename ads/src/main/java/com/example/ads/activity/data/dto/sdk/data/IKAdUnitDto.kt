package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class IKAdUnitDto(
    val adUnitId: String? = "",
    val adPriority: Int? = 0,
    val timeOut: Long? = 0,
    val label: String? = "",
    val cacheSize: Int? = 0,
) : Parcelable
