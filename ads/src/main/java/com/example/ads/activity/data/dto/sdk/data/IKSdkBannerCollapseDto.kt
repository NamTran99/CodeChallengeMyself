package com.example.ads.activity.data.dto.sdk.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_sdk_banner_cl_config")
data class IKSdkBannerCollapseDto(
    @PrimaryKey(autoGenerate = true)
    var idAuto: Int
) : IKSdkBaseDto()
