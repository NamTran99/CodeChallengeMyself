package com.example.ads.activity.data.dto.sdk

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_sdk_gk")
data class IKGkAdDto(
    @PrimaryKey(autoGenerate = true)
    var idAuto: Int? = 0,
    @ColumnInfo(name = "inter")
    var inter: IKAdapterDto? = null,
    @ColumnInfo(name = "banner")
    var banner: IKAdapterDto? = null,
    @ColumnInfo(name = "nativeAd")
    @SerializedName("native")
    var nativeAd: IKAdapterDto? = null,
    @ColumnInfo(name = "open")
    var open: IKAdapterDto? = null,
    @ColumnInfo(name = "reward")
    var reward: IKAdapterDto? = null
) : Parcelable