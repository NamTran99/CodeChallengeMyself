package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_prod_widget_config")
data class IKSdkProdWidgetDetailDto(
    @PrimaryKey
    @ColumnInfo(name = "screenName")
    val screenName: String,
    @ColumnInfo(name = "adFormat")
    var adFormat: String,
    @ColumnInfo(name = "collapsePosition")
    var collapsePosition: String? = "",
    @ColumnInfo(name = "enableCollapseBanner")
    var enableCollapseBanner: Boolean? = false,
    @ColumnInfo(name = "enable")
    val enable: Boolean? = true,
    @ColumnInfo(name = "reloadTime")
    val reloadTime: Long? = 0,
    @ColumnInfo(name = "enableBackup")
    val enableBackup: Boolean? = true,
    @ColumnInfo(name = "adSize")
    val adSize: IKAdSizeDto? = null,
    @ColumnInfo(name = "label")
    var adLabel: String? = null,
    @ColumnInfo(name = "collapseCount")
    var collapseCount: Int? = null,
) : Parcelable

