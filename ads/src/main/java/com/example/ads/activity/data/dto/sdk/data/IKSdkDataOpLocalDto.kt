package com.example.ads.activity.data.dto.sdk.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_sdk_data_o_lc")
data class IKSdkDataOpLocalDto(
    @PrimaryKey(autoGenerate = true)
    var idAuto: Int,
    @ColumnInfo(name = "validDate")
    var validDate: String
) : IKSdkBaseDto()