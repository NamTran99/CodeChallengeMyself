package com.example.ads.activity.format.rewarded

import android.app.Activity
import com.example.ads.activity.IKSdkOptions
import com.example.ads.activity.core.IKDataCoreManager
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.SDKAdPriorityDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.format.base.IKSdkBaseAdController
import com.example.ads.activity.listener.pub.IKLoadingsAdListener
import com.example.ads.activity.listener.pub.IKShowRewardAdListener
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.listener.sdk.IKSdkBaseTrackingListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.listener.sdk.IKSdkShowRewardAdListener
import com.example.ads.activity.mediation.admob.RewardAdmob
import com.example.ads.activity.mediation.applovin.IKApplovinHelper
import com.example.ads.activity.mediation.applovin.RewardedMax
import com.example.ads.activity.mediation.fairbid.IKFairBidHelper
import com.example.ads.activity.mediation.fairbid.RewardFairBid
import com.example.ads.activity.mediation.gam.RewardGam
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IKTrackingConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


object IKRewardedController : IKSdkBaseAdController() {
    private const val TAG_LOG = "IKRewardC"

    override val admob: RewardAdmob by lazy {
        RewardAdmob()
    }

    override val adMax: RewardedMax? by lazy {
        if (IKApplovinHelper.isInitialized())
            RewardedMax()
        else null
    }

    override val adGam: RewardGam by lazy {
        RewardGam()
    }
    override val adIK: IKSdkBaseAd<*>?
        get() = null

    override val adFairBid: RewardFairBid? by lazy {
        if (IKFairBidHelper.hasLib())
            RewardFairBid()
        else null
    }

    override suspend fun getBackupAd(): IKAdapterDto? = IKDataCoreManager.getOtherReward()
    override val adFormat: IKAdFormat = IKAdFormat.REWARD

    private val mTrackingListener: IKSdkBaseTrackingListener
        get() = object : IKSdkBaseTrackingListener(
            "", IKSdkDefConst.AdFormat.REWARD,
            "", ""
        ) {}

    private fun showAds(
        activity: Activity,
        screen: String,
        adsListener: IKSdkShowRewardAdListener?
    ) {
        showLogSdk("showAds") { "$screen, start run" }
        var delayJob: Job? = null

        val customAdsListener = object : IKSdkBaseListener() {
            override fun onAdsRewarded(
                adNetworkName: String,
                screen: String,
                scriptName: String,
                adUUID: String
            ) {
                adsListener?.onAdsRewarded()
                mTrackingListener.onAdsRewarded(adNetworkName, screen, scriptName, adUUID)
            }

            override fun onAdClicked(
                adNetworkName: String,
                screen: String,
                scriptName: String,
                adUUID: String
            ) {
                mTrackingListener.onAdClicked(adNetworkName, screen, scriptName, adUUID)
            }

            override fun onAdImpression(
                adNetworkName: String,
                screen: String,
                scriptName: String,
                adUUID: String
            ) {
                mTrackingListener.onAdImpression(adNetworkName, screen, scriptName, adUUID)
                showLogD("showAds") { "$screen, onAdImpression" }
            }

            override fun onAdShowed(
                adNetworkName: String,
                screen: String,
                scriptName: String,
                priority: Int,
                adUUID: String
            ) {
                isAdShowing = true
                showLogD("showAds") { "$screen, onAdReady" }
                cancelJob(delayJob)
                adsListener?.onAdsShowed(priority)
                mTrackingListener.onAdShowed(
                    adNetworkName,
                    screen,
                    scriptName,
                    priority, adUUID
                )
                if (IKSdkOptions.enablePreloadReward)
                    loadAdBase(screen, object : IKSdkLoadAdCoreListener {
                        override fun onAdLoaded() {

                        }

                        override fun onAdLoadFail(error: IKAdError) {

                        }
                    })
            }

            override fun onAdShowFailed(
                adNetworkName: String,
                screen: String,
                scriptName: String,
                error: IKAdError
            ) {
                isAdShowing = false
                showLogD("showAds") { "$screen, onAdShowFailed $error" }
                cancelJob(delayJob)
                adsListener?.onAdsShowFail(error)
                mTrackingListener.onAdShowFailed(
                    adNetworkName, screen, scriptName,
                    error
                )
                if (IKSdkOptions.enablePreloadReward)
                    loadAdBase(screen, object : IKSdkLoadAdCoreListener {
                        override fun onAdLoaded() {

                        }

                        override fun onAdLoadFail(error: IKAdError) {

                        }
                    })
            }

            override fun onAdDismissed(
                adNetworkName: String,
                screen: String,
                scriptName: String,
                adUUID: String
            ) {
                isAdShowing = false
                cancelJob(delayJob)
                mTrackingListener.onAdDismissed(adNetworkName, screen, scriptName, adUUID)
                adsListener?.onAdsDismiss()
                showLogD("showAds") { "$screen, onAdDismissed" }
            }

        }

        val totalAdObject: ArrayList<SDKAdPriorityDto> = arrayListOf()
        suspend fun addAdsToList(adNetwork: AdNetwork, timeCheck: Int) {
            val ads = when (adNetwork) {
                AdNetwork.AD_MOB -> admob.getListAdReady(false, timeCheck)
                AdNetwork.AD_MANAGER -> adGam.getListAdReady(false, timeCheck)
                AdNetwork.AD_MAX -> adMax?.getListAdReady(false, timeCheck) ?: listOf()
                AdNetwork.AD_FAIR_BID -> adFairBid?.getListAdReady(false, timeCheck) ?: listOf()
                else -> listOf()
            }
            ads.forEach { adData ->
                totalAdObject.add(
                    SDKAdPriorityDto(
                        adNetwork.value,
                        adData.adPriority,
                        adData.showPriority
                    )
                )
            }
        }
        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
            addAdsToList(AdNetwork.AD_MOB, IKSdkDefConst.TimeOutAd.REWARD)
            addAdsToList(AdNetwork.AD_MANAGER, IKSdkDefConst.TimeOutAd.REWARD)
            addAdsToList(AdNetwork.AD_MAX, IKSdkDefConst.TimeOutAd.REWARD)
            addAdsToList(AdNetwork.AD_FAIR_BID, IKSdkDefConst.TimeOutAd.REWARD)

            val maxAdPri = totalAdObject.maxByOrNull { it.adPriority }
            val maxAdObj =
                totalAdObject.filter {
                    it.adPriority >= (maxAdPri?.adPriority ?: 0)
                }.maxByOrNull { it.showPriority }

            val adsDto = maxAdObj ?: totalAdObject.firstOrNull()
            val adNetworkValue = adsDto?.adNetwork
            showLogSdk("showAds") { "$screen, process show adNetwork=$adNetworkValue" }
            when (adNetworkValue) {
                AdNetwork.AD_MOB.value -> {
                    delayJob = launch {
                        delay(DELAY_CHECK_AD)
                    }
                    adsListener?.onAdReady(adsDto.showPriority)
                    admob.showAd(
                        this,
                        activity,
                        screen,
                        customAdsListener
                    )
                    delayJob?.join()
                }

                AdNetwork.AD_MANAGER.value -> {
                    delayJob = launch {
                        delay(DELAY_CHECK_AD)
                    }
                    adsListener?.onAdReady(adsDto.showPriority)
                    adGam.showAd(
                        this,
                        activity,
                        screen,
                        customAdsListener
                    )
                    delayJob?.join()
                }


                AdNetwork.AD_MAX.value -> {
                    delayJob = launch {
                        delay(DELAY_CHECK_AD)
                    }
                    adsListener?.onAdReady(0)
                    adMax?.showAd(
                        this,
                        activity,
                        screen,
                        customAdsListener
                    )
                    delayJob?.join()
                }


                AdNetwork.AD_FAIR_BID.value -> {
                    delayJob = launch {
                        delay(DELAY_CHECK_AD)
                    }
                    adsListener?.onAdReady(0)
                    adFairBid?.showAd(
                        this,
                        activity,
                        screen,
                        customAdsListener
                    )
                    delayJob?.join()
                }


                else -> {
                    showLogSdk("showAds") { "$screen, process show no adNetwork valid!" }
                    customAdsListener.onAdShowFailed(
                        "",
                        screen,
                        "",
                        IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER)
                    )
                }
            }
        }
    }

    suspend fun isAdReady(
        targetPriority: Boolean = false
    ): Boolean {
        return when {
            admob.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.REWARD) -> {
                true
            }

            adGam.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.REWARD) -> {
                true
            }

            adMax?.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.REWARD) == true -> {
                true
            }

            adFairBid?.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.REWARD) == true -> {
                true
            }

            else -> {
                false
            }
        }
    }

    override suspend fun getAdDto(): IKSdkBaseDto? {
        repeat(4) {
            IKDataRepository.getInstance().getSDKReward()?.let {
                mAdDto = it
                return it
            }
            delay(500)
        }
        return mAdDto
    }

    suspend fun clearAdCache() {
        admob.clearAllCache()
        adGam.clearAllCache()
        adMax?.clearAllCache()
    }

    fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowRewardAdListener?,
        loadingCallback: IKLoadingsAdListener? = null
    ) {
        showLogD("showRewardedAd") { "screen=$screen start run" }
        mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
            val context = WeakReference(activity).get()
            if (context == null) {
                showLogD("showRewardedAd") { "screen=$screen error context null" }
                adListener?.onAdsShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
                return@launchWithSupervisorJob
            }
            if (!IKSdkUtilsCore.canShowAdAsync()) {
                adListener?.onAdsShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
                showLogD("showRewardedAd") { "screen=$screen error user premium" }
                return@launchWithSupervisorJob
            }
            IKSdkTrackingHelper.trackingSdkShowAd(
                adFormat = IKSdkDefConst.AdFormat.REWARD,
                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
                screen = screen
            )
            val sdkAdListener = object : IKShowRewardAdListener {
                override fun onAdsShowed() {
                    super.onAdsShowed()
                    mUiScope.launchWithSupervisorJob {
                        adListener?.onAdsShowed()
                    }
                    showLogD("showRewardedAd") { "screen=$screen onAdsShowed" }
                }

                override fun onAdsShowTimeout() {
                    super.onAdsShowTimeout()
                    mUiScope.launchWithSupervisorJob {
                        adListener?.onAdsShowTimeout()
                    }
                    showLogD("showRewardedAd") { "screen=$screen onAdsShowTimeout" }
                }

                override fun onAdsDismiss() {
                    mUiScope.launchWithSupervisorJob {
                        adListener?.onAdsDismiss()
                    }
                    showLogD("showRewardedAd") { "screen=$screen onAdsDismiss" }
                }

                override fun onAdsRewarded() {
                    mUiScope.launchWithSupervisorJob {
                        adListener?.onAdsRewarded()
                    }
                    showLogD("showRewardedAd") { "screen=$screen onAdsRewarded" }
                }

                override fun onAdsShowFail(error: IKAdError) {
                    mUiScope.launchWithSupervisorJob {
                        adListener?.onAdsShowFail(error)
                    }
                    IKSdkTrackingHelper.trackingSdkShowAd(
                        adFormat = IKSdkDefConst.AdFormat.REWARD,
                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
                        screen = screen,
                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
                    )
                    showLogD("showRewardedAd") { "screen=$screen onAdsShowFail $error" }
                }
            }
            if (IKSdkUtilsCore.isFullScreenAdShowing {
                    showLogD("showRewardedAd") { it }
                }) {
                sdkAdListener.onAdsShowFail(IKAdError(IKSdkErrorCode.OTHER_ADS_SHOWING))
                return@launchWithSupervisorJob
            }

            val configDto = mRepository?.getConfigReward(screen)

            if (configDto == null || configDto.enable != true) {
                val errorCode = if (configDto == null)
                    IKSdkErrorCode.NO_SCREEN_ID_AD
                else IKSdkErrorCode.DISABLE_SHOW

                sdkAdListener.onAdsShowFail(IKAdError(errorCode))
                return@launchWithSupervisorJob
            }
            if (loadingCallback != null && loadingCallback.timeLoading >= 500) {
                showLogD("showRewardedAd") { "screen=$screen showLoading start" }
                showAds(
                    context,
                    screen,
                    object : IKSdkShowRewardAdListener {
                        override fun onAdsRewarded() {
                            sdkAdListener.onAdsRewarded()
                        }

                        override fun onAdsShowed(priority: Int) {
                            mUiScope.launchWithSupervisorJob {
                                loadingCallback.onClose()
                            }
                            sdkAdListener.onAdsShowed()
                        }

                        override suspend fun onAdReady(priority: Int) {
                            mUiScope.launchWithSupervisorJob {
                                loadingCallback.onShow()
                            }
                            showLogD("showRewardedAd") { "screen=$screen showLoading call show" }
                            delay(loadingCallback.timeLoading)
                        }

                        override fun onAdsDismiss() {
                            mUiScope.launchWithSupervisorJob {
                                loadingCallback.onClose()
                            }
                            sdkAdListener.onAdsDismiss()

                        }

                        override fun onAdsShowFail(error: IKAdError) {
                            mUiScope.launchWithSupervisorJob {
                                loadingCallback.onClose()
                                sdkAdListener.onAdsShowFail(error)
                            }

                        }
                    }
                )
                return@launchWithSupervisorJob
            }
            showLogD("showRewardedAd") { "screen=$screen noneLoading start" }
            IKRewardedController.showAds(
                context, screen, object : IKSdkShowRewardAdListener {
                    override fun onAdsRewarded() {
                        sdkAdListener.onAdsRewarded()
                    }

                    override fun onAdsShowed(priority: Int) {
                        sdkAdListener.onAdsShowed()
                    }

                    override suspend fun onAdReady(priority: Int) {

                    }

                    override fun onAdsDismiss() {
                        sdkAdListener.onAdsDismiss()
                    }

                    override fun onAdsShowFail(error: IKAdError) {
                        sdkAdListener.onAdsShowFail(error)
                    }
                }
            )

        }
    }

    fun loadAd(
        screenAd: String,
        callback: IKSdkLoadAdCoreListener?
    ) {
        loadAdBase(screenAd, object : IKSdkLoadAdCoreListener {
            override fun onAdLoaded() {
                mUiScope.launchWithSupervisorJob {
                    callback?.onAdLoaded()
                }
            }

            override fun onAdLoadFail(error: IKAdError) {
                mUiScope.launchWithSupervisorJob {
                    callback?.onAdLoadFail(error)
                }
            }
        })
    }

    private fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk(TAG_LOG) {
            "${tag}:" + message.invoke()
        }
    }

    private fun showLogD(tag: String, message: () -> String) {
        IKLogs.dSdk(TAG_LOG) {
            "${tag}:" + message.invoke()
        }
    }
}