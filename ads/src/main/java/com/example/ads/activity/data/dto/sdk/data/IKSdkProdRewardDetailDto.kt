package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_prod_reward_config")
data class IKSdkProdRewardDetailDto(
    @ColumnInfo(name = "screenName")
    val screenName: String,
    @ColumnInfo(name = "enable")
    val enable: Boolean? = true
) : Parcelable

