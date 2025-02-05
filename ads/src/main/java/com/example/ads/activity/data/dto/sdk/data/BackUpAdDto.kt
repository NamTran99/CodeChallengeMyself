package com.example.ads.activity.data.dto.sdk.data

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class BackUpAdDto(
    @SerializedName("adsName")
    val adsName: String = ""
) : IKAdUnitDto()