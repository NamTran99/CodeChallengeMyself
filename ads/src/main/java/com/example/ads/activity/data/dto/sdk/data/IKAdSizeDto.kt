package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class IKAdSizeDto(
    @SerializedName("adType")
    val adType: String? = "",
    @SerializedName("width")
    val width: Int? = 0,
    @SerializedName("height")
    val height: Int? = 0
) : Parcelable
