package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class IKAdapterDto(
    @ColumnInfo(name = "adNetwork")
    @SerializedName("adNetwork")
    var adNetwork: String? = "",
    @ColumnInfo(name = "enable")
    @SerializedName("enable")
    var enable: Boolean = true,
    @ColumnInfo(name = "showPriority")
    @SerializedName("showPriority")
    var showPriority: Int? = 0,
    @ColumnInfo(name = "loadMode")
    @SerializedName("loadMode")
    var loadMode: String? = "",
    @ColumnInfo(name = "maxQueue")
    @SerializedName("maxQueue")
    var maxQueue: Int? = 0,
    @ColumnInfo(name = "adData")
    @SerializedName("adData")
    var adData: MutableList<IKAdUnitDto>? = null,
    @ColumnInfo(name = "label")
    @SerializedName("label")
    var label: String? = null,
    @ColumnInfo(name = "enablePreload")
    @SerializedName("enablePreload")
    var enablePreload: Boolean? = false,
    @ColumnInfo(name = "enablePreloadManually")
    @SerializedName("enablePreloadManually")
    var enablePreloadManually: Boolean? = false,
    @ColumnInfo(name = "appKey")
    @SerializedName("appKey")
    var appKey: String? = null,
    @ColumnInfo(name = "des")
    @SerializedName("des")
    var des: String? = null,
    @ColumnInfo(name = "disableLoadAndShow")
    @SerializedName("disableLoadAndShow")
    var disableLoadAndShow: Boolean? = false,
    @ColumnInfo(name = "autoClm")
    @SerializedName("autoClm")
    var autoClm: Boolean? = false,
    @ColumnInfo(name = "customTag")
    @SerializedName("customTag")
    var customTag: String? = null,
) : Parcelable {
    fun getSingle(): IKAdUnitDto? {
        return adData?.firstOrNull()
    }
}