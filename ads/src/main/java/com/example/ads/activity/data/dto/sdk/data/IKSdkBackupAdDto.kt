package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_sdk_default_config")
data class IKSdkBackupAdDto(
    @PrimaryKey(autoGenerate = true)
    var idAuto: Int? = 0,
    @ColumnInfo(name = "adConfigs")
    var adConfigs: MutableMap<String, IKAdapterDto>? = null
) : Parcelable {
    fun getConfigByType(type: String): IKAdapterDto? {
        return adConfigs?.get(type)
    }

    fun getInterConfig(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.INTER)
    fun getBannerConfig(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.BANNER)
    fun getNativeAdConfig(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.NATIVE)
    fun getOpenConfig(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.OPEN)
    fun getRewardConfig(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.REWARD)
    fun getBannerInlineConfig(): IKAdapterDto? =
        getConfigByType(IKSdkDefConst.AdFormat.BANNER_INLINE)

    fun getBannerCollapseConfig(): IKAdapterDto? =
        getConfigByType(IKSdkDefConst.AdFormat.BANNER_COLLAPSE)

    fun getNativeFullConfig(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.NATIVE_FULL)
    fun getBannerCollapseBanner(): IKAdapterDto? = getConfigByType(IKSdkDefConst.AdFormat.BN_CL_BN)
    fun getBannerCollapseInline(): IKAdapterDto? =
        getConfigByType(IKSdkDefConst.AdFormat.BN_CL_BN_IN)

}