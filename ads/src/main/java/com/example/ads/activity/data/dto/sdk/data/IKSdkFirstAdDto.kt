package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_prod_first_config")
data class IKSdkFirstAdDto(
    @PrimaryKey(autoGenerate = true)
    var idAuto: Int,
    @ColumnInfo(name = "adFormat")
    var adsFormat: String?,
    @ColumnInfo(name = "adNetwork")
    var adsNetwork: String?,
    @ColumnInfo(name = "backupAdFormat")
    var backupAdFormat: String?,
    @ColumnInfo(name = "backupAdEnable")
    var backupAdEnable: Boolean?,
    @ColumnInfo(name = "timeOut")
    var timeOut: Long? = 0L,
    @ColumnInfo(name = "timeExtend")
    var timeExtend: Long? = 0L,
    @ColumnInfo(name = "timeReload")
    var timeReload: Long? = 0L,
    @ColumnInfo(name = "timeOutSoon")
    var timeOutSoon: Long? = 0L,
    @ColumnInfo(name = "timeOutWaitLoading")
    var timeOutWaitLoading: Long? = 0L,
    @ColumnInfo(name = "enableTimeOutWaitLoading")
    var enableTimeOutWaitLoading: Boolean? = false,
    @ColumnInfo(name = "disableAd")
    var disableAd: Boolean? = false,
    @ColumnInfo(name = "disableDataLocal")
    var disableDataLocal: Boolean? = false,
    @ColumnInfo(name = "enableBid")
    var enableBid: Boolean? = true,
    @ColumnInfo(name = "customLabel")
    var customLabel: String? = null
) : Parcelable
