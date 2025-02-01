package com.example.ads.activity.billing.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IKSdkBillingDto(
    var screen: String? = null,
    var data: ArrayList<IKSdkBillingDataDto>? = null
) : Parcelable
