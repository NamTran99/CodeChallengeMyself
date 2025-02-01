package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IKMapShowInterDto(
    val screenName: String,
    var isFirstTime: Boolean,
    var timeShow: Long,
    var countSinceLastAd: Int = 1,
    var showAdFrequency: Int = 1
) : Parcelable