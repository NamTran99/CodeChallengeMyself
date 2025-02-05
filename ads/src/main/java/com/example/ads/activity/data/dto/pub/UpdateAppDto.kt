package com.example.ads.activity.data.dto.pub

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateAppDto(
    val currentVersionCode: Long = 0,
    val versionValue: String = "",
    val minVersionCode: Long = 0,
    val minVersionValue: String = "",
    val forceUpdateApp: Boolean = false,
    val directLink: String? = ""
) : Parcelable
