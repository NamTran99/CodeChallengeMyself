package com.example.ads.activity.core

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.core.IKDataCoreManager.updateAppConfig
import com.example.ads.activity.core.firebase.IKRemoteDataManager
//import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.*
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.pub.UpdateAppDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkInterDto
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.listener.keep.SDKNewVersionUpdateCallback
//import com.example.ads.activity.mediation.playgap.IKPlayGapHelper
import com.example.ads.activity.utils.*
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IkmSdkCoreFunc.SdkF.mContainAdActivity
import kotlinx.coroutines.*
import java.util.*

abstract class CoreController {

    companion object {
        const val BLOCK_TIME_FORGE_UPDATE = 300L

    }

    private var mNeedToReloadAds = true
    var firstAdsType: String = IKSdkConstants.defaultFirstAdType
    var disableFirstAd: Boolean = false
        protected set

    val mRepository: IKDataRepository? by lazy {
        IKDataRepository.getInstance()
    }

    private val mConfigJob = SupervisorJob()
    protected val mUiScope = CoroutineScope(Dispatchers.Main + mConfigJob)

    fun initConfig(context: Context) {
        showLogD("initConfig") { "start" }
        mNeedToReloadAds = true
        IKRemoteDataManager.initFirebaseRemoteConfig()
    }


    private fun addContainAdActivity() {
        mContainAdActivity.add("ads.AdActivity")
        mContainAdActivity.add("AudienceNetworkActivity")
        mContainAdActivity.add("UnInstallDialog")
        mContainAdActivity.add("bytedance")
        mContainAdActivity.add("OfferWallActivity")
        mContainAdActivity.add("mbridge")
        mContainAdActivity.add("CBImpressionActivity")
        mContainAdActivity.add("CBImpressionActivity")
        mContainAdActivity.add("vungle")
        mContainAdActivity.add("adcolony")
        mContainAdActivity.add("AdUnitActivity")
        mContainAdActivity.add("AdUnitTransparentActivity")
        mContainAdActivity.add("AdUnitTransparentSoftwareActivity")
        mContainAdActivity.add("AdUnitSoftwareActivity")
        mContainAdActivity.add("FullScreenWebViewDisplay")
        mContainAdActivity.add("InMobiAdActivity")
        mContainAdActivity.add("tapjoy")
        mContainAdActivity.add("AppLovinFullscreenThemedActivity")
        mContainAdActivity.add("AppLovinFullscreenActivity")
        mContainAdActivity.add("AppLovinFullscreen")
        mContainAdActivity.add("FullScreenActivity")
        mContainAdActivity.add("VideoPlayerActivity")
        mContainAdActivity.add("VungleActivity")
        mContainAdActivity.add("TTRewardExpressVideoActivity")
        mContainAdActivity.add("TTRewardVideoActivity")
        mContainAdActivity.add("TTAppOpenAdActivity")
        mContainAdActivity.add("TTFullScreenVideoActivity")
        mContainAdActivity.add("TTFullScreenExpressVideoActivity")
        mContainAdActivity.add("TTInterstitialActivity")
        mContainAdActivity.add("TTInterstitialExpressActivity")
        mContainAdActivity.add("RewardedInterstitialAdActivity")
        mContainAdActivity.add("InterstitialAdActivity")
        mContainAdActivity.add("InterstitialActivity")
        mContainAdActivity.add("DTBInterstitialActivity")
        mContainAdActivity.add("DTBAdActivity")
        mContainAdActivity.add("AdSplashActivity")
        mContainAdActivity.add("LandscapeAdSplashActivity")
        mContainAdActivity.add("AdFormActivity")
        mContainAdActivity.add("AdActivity")
        mContainAdActivity.add("CompanionAdActivity")
        mContainAdActivity.add("InneractiveFullscreenAdActivity")
        mContainAdActivity.add("IkmOpenAdActivity")
        mContainAdActivity.add("IkmInterAdActivity")
        mContainAdActivity.add("IKameAdActivity")
    }

    fun addContainAdActivity(activityName: String) {
        mContainAdActivity.add(activityName)
    }

    fun removeContainAdActivity(activityName: String) {
        mContainAdActivity.remove(activityName)
    }

    fun initAdsConfig(context: Context?) {
        showLogD("initAdsConfig") { "start" }
        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            kotlin.runCatching {
                val result = IKSdkConstants.defaultFirstAdDto
                val interSplash =
                    IKSdkUtilsCore.checkInterSplashCountryCode(IKSdkApplicationProvider.getContext())
                if (interSplash)
                    result.adsFormat = IKAdFormat.INTER.value
                mRepository?.insertCacheSDKFirstAd(result)
            }
        }
        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            getFirstAdType()
            IKRemoteDataManager.getDataCache(context)
        }
        addContainAdActivity()
    }

    suspend fun getFirstAdType() {
        firstAdsType = mRepository?.getSDKFirstAd()?.adsFormat ?: IKSdkDefConst.EMPTY
        if (firstAdsType.isBlank()) firstAdsType = IKSdkConstants.defaultFirstAdType
        disableFirstAd = mRepository?.getSDKFirstAd()?.disableAd ?: false
        showLogD("getFirstAdType") { "refresh=$firstAdsType" }
    }

    open suspend fun isAnOtherAdsShowing(): Boolean {
        return IKSdkUtilsCore.isFullScreenAdShowing {
            showLogD("isAnOtherAdsShowing") { it }
        }
    }

    /**
     * Checks if an app update is available.
     *
     * @param activity The activity where the check is being performed.
     * @param callbackUpdate A callback function that will be called with a boolean value indicating
     * whether an update is available or not.
     */
    fun checkUpdateApp(
        callbackUpdate: SDKNewVersionUpdateCallback?
    ) {
        mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
            val updateDto: UpdateAppDto? = null
            val hasUpdate = withContext(Dispatchers.Default) {
                showLogD("checkUpdateApp") { "start run" }
                try {
                    var cacheConfig = updateAppConfig
                    kotlin.runCatching {
                        if (updateAppConfig.isBlank())
                            cacheConfig =
                                IKRemoteDataManager.getRemoteConfigData()[IKSdkDefConst.Config.UPDATE_APP_CONFIG]?.getString()
                                    ?: IKSdkDefConst.EMPTY
                    }
                    if (cacheConfig.isNotBlank()) {
                        val updateAppDto = Gson().fromJson(cacheConfig, UpdateAppDto::class.java)
                        try {
                            val version =
                                IKSdkUtilsCore.getVersionApp(IKSdkApplicationProvider.getContext())
                            if (updateAppDto.minVersionCode > version) {
                                return@withContext true
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                        }
                    }

                } catch (e: Exception) {
                    showLogD("checkUpdateApp") { "error ${e.message}" }
                }
                return@withContext false
            }
            showLogD("checkUpdateApp") { "hasUpdate $hasUpdate" }
            if (hasUpdate)
                callbackUpdate?.onUpdateAvailable(updateDto)
            else callbackUpdate?.onUpdateFail()
        }
    }

    private fun showLogD(tag: String, message: () -> String) {
        IKLogs.dSdk("CCController") {
            "${tag}:" + message.invoke()
        }
    }

    fun initClaimAd() {
        mUiScope.launchWithSupervisorJob {
            withContext(Dispatchers.Default) {
                var mAdDto: IKSdkInterDto? = null
                mRepository?.getSDKInter()?.let {
                    mAdDto = it
                }
                if (mAdDto == null) {
                    delay(1000)
                    mRepository?.getSDKInter()?.let {
                        mAdDto = it
                    }
                }
                if (mAdDto == null) {
                    delay(1000)
                    mRepository?.getSDKInter()?.let {
                        mAdDto = it
                    }
                }
                val playGapDto =
                    mAdDto?.adapters?.find { it.enable && it.adNetwork == AdNetwork.PLAYGAP.value }
                if (playGapDto != null && playGapDto.appKey?.isNotBlank() == true) {
                    IKPlayGapHelper.initialize(playGapDto.appKey ?: "")
                }
            }
        }
    }

    protected fun initializeMobileAdsSdk() {
        showLogSdk("initializeMobileAdsSdk") { "cmp init sdk start run" }
        mUiScope.launchWithSupervisorJob {
            val request = IKSdkDataStore.getBoolean(IKSdkDataStoreConst.KEY_CMP_STATUS, false)
            if (request) {
                showLogSdk("initializeMobileAdsSdk") { "cmp init sdk start request =true" }
                return@launchWithSupervisorJob
            }
            IKSdkDataStore.putBoolean(IKSdkDataStoreConst.KEY_CMP_STATUS, true)
            withContext(Dispatchers.IO) {
                IKSdkApplicationProvider.getContext()?.let {
                    MobileAds.initialize(it)
                }
            }
        }
    }

    protected fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk("IKSdkController") {
            "${tag}:" + message.invoke()
        }
    }
}
