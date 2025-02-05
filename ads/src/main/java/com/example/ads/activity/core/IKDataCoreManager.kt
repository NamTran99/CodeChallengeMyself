package com.example.ads.activity.core

//import com.example.ads.activity.BuildConfig
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IKDataCoreManager {
    private var backupBanner: IKAdapterDto? = null
    private var backupNative: IKAdapterDto? = null
    private var backupInter: IKAdapterDto? = null
    private var backupOpen: IKAdapterDto? = null
    private var backupReward: IKAdapterDto? = null
    private var backupBannerInline: IKAdapterDto? = null
    private var backupBannerCollapse: IKAdapterDto? = null
    private var backupBannerCollapseBanner: IKAdapterDto? = null
    private var backupBannerCollapseInline: IKAdapterDto? = null
    var otherConfig: HashMap<String, Any> = hashMapOf()
        private set
    var updateAppConfig: String = IKSdkDefConst.EMPTY
    var ikDmLv1 = false
    var ikDmLvSdk = false
    var ikDmLv = ""

//    fun isEnableDebug(): Boolean {
//        return ikDmLv1 || ikDmLvSdk || BuildConfig.DEBUG
//    }

//    fun isEnableTracking(): Boolean {
//        return ikDmLv == IKSdkDefConst.Logs.Level.LEVEL_TRACKING || isEnableDebug()
//    }

    fun setOtherConfig(value: HashMap<String, Any>?, isClear: Boolean = false) {
        if (isClear) {
            otherConfig = hashMapOf()
            return
        }
        if (!value.isNullOrEmpty())
            otherConfig = value
    }

    suspend fun getOtherBannerAds(): IKAdapterDto? =
        withContext(Dispatchers.Default) {
            val config = IKDataRepository.getBackupDto()?.getBannerConfig()
            if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
                config
            } else {
//                if (backupBanner == null)
//                    backupBanner = kotlin.runCatching {
//                        SDKDataHolder.getObjectSdk(
//                            IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.BANNER],
//                            IKAdapterDto::class.java
//                        )
//                    }.getOrNull()
                backupBanner
            }
        }

    suspend fun getOtherBannerCollapseAds(): IKAdapterDto? =
        withContext(Dispatchers.Default) {
            val config = IKDataRepository.getBackupDto()?.getBannerCollapseConfig()
            if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
                config
            } else {
//                if (backupBannerCollapse == null)
//                    backupBannerCollapse = kotlin.runCatching {
//                        SDKDataHolder.getObjectSdk(
//                            IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.BANNER_COLLAPSE],
//                            IKAdapterDto::class.java
//                        )
//                    }.getOrNull()
                backupBannerCollapse
            }
        }


    suspend fun getOtherNativeAds(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getNativeAdConfig()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupNative == null)
//                backupNative = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.NATIVE],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupNative
        }
    }

    suspend fun getOtherOpenAds(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getOpenConfig()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupOpen == null)
//                backupOpen = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.OPEN],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupOpen
        }
    }


    suspend fun getOtherInterAds(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getInterConfig()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupInter == null)
//                backupInter = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.INTER],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupInter
        }
    }

    suspend fun getOtherReward(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getRewardConfig()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupReward == null)
//                backupReward = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.REWARD],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupReward
        }
    }

    suspend fun getOtherBannerInline(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getBannerInlineConfig()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupBannerInline == null)
//                backupBannerInline = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.BANNER_INLINE],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupBannerInline
        }
    }

    suspend fun getOtherNativeFullAds(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getNativeFullConfig()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupNative == null)
//                backupNative = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.NATIVE],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupNative ?: getOtherNativeAds()
        }
    }

    suspend fun getBackupBannerCollapseBanner(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getBannerCollapseBanner()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupBannerCollapseBanner == null)
//                backupBannerCollapseBanner = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.BN_CL_BN],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupBannerCollapseBanner
        }
    }

    suspend fun getBackupBannerCollapseInline(): IKAdapterDto? = withContext(Dispatchers.Default) {
        val config = IKDataRepository.getBackupDto()?.getBannerCollapseInline()
        if (!config?.getSingle()?.adUnitId.isNullOrBlank()) {
            config
        } else {
//            if (backupBannerCollapseInline == null)
//                backupBannerCollapseInline = kotlin.runCatching {
//                    SDKDataHolder.getObjectSdk(
//                        IKSdkConstants.BACKUP_CONFIG[IKSdkDefConst.AdFormat.BN_CL_BN_IN],
//                        IKAdapterDto::class.java
//                    )
//                }.getOrNull()
            backupBannerCollapseInline
        }
    }

}