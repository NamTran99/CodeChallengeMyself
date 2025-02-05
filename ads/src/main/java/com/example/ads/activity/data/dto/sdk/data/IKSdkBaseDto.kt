package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
open class IKSdkBaseDto(
    @ColumnInfo(name = "loadMode")
    var loadMode: String? = "",
    @ColumnInfo(name = "maxQueue")
    var maxQueue: Int? = 0,
    @ColumnInfo(name = "label")
    var label: String? = "",
    @ColumnInfo(name = "adapters")
    var adapters: ArrayList<IKAdapterDto>? = null
) : Parcelable
