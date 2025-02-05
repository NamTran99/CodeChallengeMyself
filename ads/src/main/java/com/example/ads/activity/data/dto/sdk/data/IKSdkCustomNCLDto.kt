package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_sdk_custom_ncl_config")
data class IKSdkCustomNCLDto(
    @PrimaryKey(autoGenerate = true)
    var idAuto: Int,
    @ColumnInfo(name = "label")
    var label: String? = "",
    @ColumnInfo(name = "data")
    val data: List<IKSdkCustomNCLDetailDto>? = null
) : Parcelable
