package com.example.ads.activity.format.native_ads

import com.example.ads.activity.core.SDKDataHolder
//import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.db.IkmSdkCacheFunc.DB.verifyCache
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object NativeBackUpLatest {
    private val mJob = SupervisorJob()
    private val mUiScope = CoroutineScope(Dispatchers.Main + mJob)

    private var backupAdLatest: IKSdkBaseLoadedAd<*>? = null
    private var cacheConfig: Pair<Long, HashMap<String, Any>?> =
        Pair(0, null)

    private fun getCacheConfig(): HashMap<String, Any>? {
        mUiScope.launchWithSupervisorJob {
//            val newData = cacheConfig.verifyCache {
//                runCatching {
//                    SDKDataHolder.getObjectSdk<HashMap<String, Any>>(
//                        IKRemoteDataManager.getRemoteConfigData()[IKSdkDefConst.Config.SDK_BACKUP_NATIVE_LATEST]?.value,
//                        HashMap::class.java
//                    )
//                }.getOrNull()
//            }

//            if (newData?.second == null) {
//                cacheConfig.second
//            } else {
//                cacheConfig = newData
//                cacheConfig.second
//            }
        }

        return cacheConfig.second
    }

    fun getCurrentBackupAd(): IKSdkBaseLoadedAd<*>? {
        return backupAdLatest
    }

    fun getBackupAd(adFormat: IKAdFormat): IKSdkBaseLoadedAd<*>? {
        return when (adFormat) {
            IKAdFormat.INTER -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.INTER) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.OPEN -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.OPEN) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.BANNER -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.BANNER) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.NATIVE -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.NATIVE) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.NATIVE_FULL -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.NATIVE_FULL) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.REWARD -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.REWARD) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.NATIVE_BANNER -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.NATIVE_BANNER) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.BANNER_COLLAPSE -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.COLLAPSE) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.BANNER_COLLAPSE_C1_BN -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.COLLAPSE_C1) == true)
                    backupAdLatest
                else
                    null
            }

            IKAdFormat.BANNER_COLLAPSE_C2 -> {
                if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.COLLAPSE_C2) == true)
                    backupAdLatest
                else
                    null
            }

            else -> null
        }

    }

    /**
     * Check ad priority
     * Destroy old loaderAd if it isn't displaying and cache new loaderAd
     * */
    fun addBackupAdLatest(ad: IKSdkBaseLoadedAd<*>?) {
        mUiScope.launchWithSupervisorJob {
            if (getCacheConfig()?.get(IKSdkDefConst.NativeBackUpLatest.ENABLE) != true)
                return@launchWithSupervisorJob
            if (ad == null)
                return@launchWithSupervisorJob

            if (backupAdLatest == null) {
                backupAdLatest = ad
                return@launchWithSupervisorJob
            }

            backupAdLatest?.let {
                if (ad.adPriority > it.adPriority) {
                    if (!it.isDisplayAdView) {
                        it.destroyAd()
                    }
                    backupAdLatest = ad
                }
            }
        }
    }
}