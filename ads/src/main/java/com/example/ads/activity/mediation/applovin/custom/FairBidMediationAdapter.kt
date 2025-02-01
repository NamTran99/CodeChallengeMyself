//package com.example.ads.activity.mediation.applovin.custom
//
//import android.app.Activity
//import android.content.Context
//import android.os.Bundle
//import android.view.ViewGroup
//import android.widget.RelativeLayout
//import com.applovin.mediation.MaxAdFormat
//import com.applovin.mediation.adapter.MaxAdViewAdapter
//import com.applovin.mediation.adapter.MaxAdapter
//import com.applovin.mediation.adapter.MaxAdapterError
//import com.applovin.mediation.adapter.MaxInterstitialAdapter
//import com.applovin.mediation.adapter.MaxRewardedAdapter
//import com.applovin.mediation.adapter.MaxSignalProvider
//import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener
//import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener
//import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener
//import com.applovin.mediation.adapter.listeners.MaxSignalCollectionListener
//import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters
//import com.applovin.mediation.adapter.parameters.MaxAdapterParameters
//import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters
//import com.applovin.mediation.adapter.parameters.MaxAdapterSignalCollectionParameters
//import com.applovin.mediation.adapters.MediationAdapterBase
//import com.applovin.sdk.AppLovinSdk
//import com.applovin.sdk.AppLovinSdkUtils
////import com.fyber.FairBid
////import com.fyber.fairbid.ads.Banner
////import com.fyber.fairbid.ads.Rewarded
////import com.fyber.fairbid.ads.banner.BannerError
////import com.fyber.fairbid.ads.banner.BannerListener
////import com.fyber.fairbid.ads.banner.BannerOptions
////import com.fyber.fairbid.ads.banner.BannerSize
////import com.fyber.fairbid.ads.rewarded.RewardedListener
////import com.fyber.fairbid.user.UserInfo
////import com.fyber.inneractive.sdk.external.BidTokenProvider
////import com.fyber.inneractive.sdk.external.InneractiveErrorCode
//import com.google.gson.reflect.TypeToken
//import com.example.ads.activity.core.SDKDataHolder
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKCustomEventData
//import com.example.ads.activity.listener.sdk.IKSdkCustomEventAdListener
//import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import com.example.ads.activity.mediation.custom.IKCustomEventInitListener
//import com.example.ads.activity.mediation.custom.fairbid.IKFairBid
//import com.example.ads.activity.mediation.custom.fairbid.IKFairBidController
//import com.example.ads.activity.utils.IKLogs
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.withContext
//import java.util.concurrent.atomic.AtomicBoolean
//
//class FairBidMediationAdapter
//    (sdk: AppLovinSdk?) : MediationAdapterBase(sdk), MaxSignalProvider, MaxInterstitialAdapter,
//    MaxRewardedAdapter, MaxAdViewAdapter {
//    private var adViewGroup: ViewGroup? = null
//    private var hasGrantedReward = false
//    private var bannerUnit = ""
//    private var rewardedUnit = ""
//    private var isBannerAdLoading = false
//    private var controller: IKFairBidController? = IKFairBidController()
//    private fun logAd(value: String) {
//        IKLogs.d("FairBidMediationAdapter") { value }
//    }
//
//    override fun initialize(
//        parameters: MaxAdapterInitializationParameters,
//        activity: Activity,
//        onCompletionListener: MaxAdapter.OnCompletionListener
//    ) {
//        val appId = parameters.serverParameters.getString("app_id", null)
//        IKFairBid.initialize(
//            activity,
//            appId,
//            wrappingSdk.userIdentifier,
//            object : IKCustomEventInitListener {
//                override fun onSuccess() {
//                    onCompletionListener.onCompletion(
//                        MaxAdapter.InitializationStatus.INITIALIZED_SUCCESS,
//                        null
//                    )
//                }
//
//                override fun onFail(error: IKAdError) {
//                    onCompletionListener.onCompletion(
//                        MaxAdapter.InitializationStatus.INITIALIZED_FAILURE,
//                        error.message
//                    )
//                }
//            })
//    }
//
//    override fun getSdkVersion(): String {
//        return FairBid.getAgpVersion() ?: "1.0.0"
//    }
//
//    override fun getAdapterVersion(): String {
//        return "1.0.0"
//    }
//
//    override fun onDestroy() {
//        adViewGroup = null
//        controller?.destroy()
//        controller = null
//    }
//
//    override fun collectSignal(
//        parameters: MaxAdapterSignalCollectionParameters,
//        activity: Activity,
//        callback: MaxSignalCollectionListener
//    ) {
//        logAd("Collecting signal...")
//        updateUserInfo(parameters)
//        val signal = BidTokenProvider.getBidderToken()
//        if (signal != null) {
//            callback.onSignalCollected(signal)
//        } else {
//            logAd("Failed to collect signal")
//            callback.onSignalCollectionFailed(null)
//        }
//    }
//
//    override fun loadInterstitialAd(
//        parameters: MaxAdapterResponseParameters,
//        activity: Activity,
//        listener: MaxInterstitialAdapterListener
//    ) {
//        logAd("Loading " + (if (AppLovinSdkUtils.isValidString(parameters.bidResponse)) "bidding " else "") + "interstitial ad for spot id " + parameters.thirdPartyAdPlacementId + "\"...")
//        val scope = CoroutineScope(Dispatchers.Main)
//        updateUserInfo(parameters)
//        scope.launchWithSupervisorJob {
//            var listUnitInter = mutableListOf<IKCustomEventData>()
//            listUnitInter = withContext(Dispatchers.Default) {
//                runCatching {
//                    SDKDataHolder.getObject<List<IKCustomEventData>>(
//                        parameters.customParameters.getString("value"),
//                        object : TypeToken<List<IKCustomEventData>>() {}.type
//                    )?.sortedByDescending { it.p ?: 0 }?.toMutableList()
//                }.getOrNull() ?: mutableListOf()
//            }
//            if (listUnitInter.isEmpty()) {
//                if (!parameters.thirdPartyAdPlacementId.isNullOrBlank()) {
//                    listUnitInter.add(
//                        IKCustomEventData(
//                            0,
//                            parameters.thirdPartyAdPlacementId,
//                            IKSdkDefConst.TimeOutAd.LOAD_AD_TIME_OUT
//                        )
//                    )
//                }
//            }
//            if (listUnitInter.isEmpty()) {
//                if (controller?.isInterAdReady() == true)
//                    listener.onInterstitialAdLoaded()
//                else {
//                    val adapterError = toMaxError(InneractiveErrorCode.INVALID_INPUT)
//                    listener.onInterstitialAdLoadFailed(adapterError)
//                }
//                return@launchWithSupervisorJob
//            }
//            controller?.loadInterstitialAd(listUnitInter, object : IKSdkCustomEventAdListener {
//                override fun onAdLoaded() {
//                    listener.onInterstitialAdLoaded()
//                }
//
//                override fun onAdLoadFail(error: IKAdError) {
//                    val adapterError =
//                        MaxAdapterError(
//                            -4205,
//                            "Ad Display Failed",
//                            0,
//                            error.message
//                        )
//                    log("Interstitial failed to show: $adapterError")
//
//                    listener.onInterstitialAdDisplayFailed(adapterError)
//                }
//
//                override fun onAdClicked() {
//                    logAd("Interstitial onClick")
//                    listener.onInterstitialAdClicked()
//                }
//
//                override fun onAdImpression() {
//
//                }
//
//                override fun onAdShowed(bundle: Bundle?) {
//                    logAd("Interstitial onShow")
//                    if (bundle != null) {
//                        listener.onInterstitialAdDisplayed(bundle)
//                    } else {
//                        listener.onInterstitialAdDisplayed()
//                    }
//                }
//
//                override fun onAdShowFailed(error: IKAdError) {
//                    logAd("Interstitial ad not ready")
//                    listener.onInterstitialAdDisplayFailed(
//                        MaxAdapterError(
//                            -4205,
//                            "Ad Display Failed",
//                            0,
//                            "Interstitial ad not ready"
//                        )
//                    )
//                }
//
//                override fun onAdDismissed() {
//                    logAd("Interstitial onHide")
//                    listener.onInterstitialAdHidden()
//                }
//            })
//        }
//    }
//
//    override fun showInterstitialAd(
//        parameters: MaxAdapterResponseParameters,
//        activity: Activity,
//        listener: MaxInterstitialAdapterListener
//    ) {
//        logAd("Showing interstitial ad...")
//        if (controller == null) {
//            logAd("Interstitial ad not ready")
//            listener.onInterstitialAdDisplayFailed(
//                MaxAdapterError(
//                    -4205,
//                    "Ad Display Failed",
//                    0,
//                    "Interstitial ad not ready"
//                )
//            )
//            return
//        }
//        controller?.showAd(activity, object : IKSdkShowAdListener {
//            override fun onAdShowed(priority: Int) {
//
//            }
//
//            override suspend fun onAdReady(priority: Int) {
//            }
//
//            override fun onAdDismiss() {
//
//            }
//
//            override fun onAdShowFail(error: IKAdError) {
//                listener.onInterstitialAdDisplayFailed(
//                    MaxAdapterError(
//                        -4205,
//                        "Ad Display Failed",
//                        0,
//                        "Interstitial ad not ready"
//                    )
//                )
//            }
//        })
//
//    }
//
//    override fun loadRewardedAd(
//        parameters: MaxAdapterResponseParameters,
//        activity: Activity,
//        listener: MaxRewardedAdapterListener
//    ) {
//        logAd("Loading " + (if (AppLovinSdkUtils.isValidString(parameters.bidResponse)) "bidding " else "") + "rewarded ad for spot id " + parameters.thirdPartyAdPlacementId + "\"...")
//        rewardedUnit = parameters.thirdPartyAdPlacementId
//
//        updateUserInfo(parameters)
//        Rewarded.setRewardedListener(object : RewardedListener {
//            override fun onShow(
//                placementId: String,
//                impressionData: com.fyber.fairbid.ads.ImpressionData
//            ) {
//                logAd("Rewarded ad shown")
//
//                // Passing extra info such as creative id supported in 9.15.0+
//                val creativeId = impressionData.creativeId
//                if (!creativeId.isNullOrBlank()) {
//                    val extraInfo = Bundle(1)
//                    extraInfo.putString("creative_id", creativeId)
//                    listener.onRewardedAdDisplayed(extraInfo)
//                } else {
//                    listener.onRewardedAdDisplayed()
//                }
//            }
//
//            override fun onClick(placementId: String) {
//                logAd("Rewarded ad clicked")
//                listener.onRewardedAdClicked()
//            }
//
//            override fun onHide(placementId: String) {
//                if (hasGrantedReward || shouldAlwaysRewardUser()) {
//                    val reward = reward
//                    logAd("Rewarded user with reward: $reward")
//                    listener.onUserRewarded(reward)
//                }
//                logAd("Rewarded ad hidden")
//                listener.onRewardedAdHidden()
//            }
//
//            override fun onShowFailure(
//                placementId: String,
//                impressionData: com.fyber.fairbid.ads.ImpressionData
//            ) {
//                // Called when an error arises when showing the rewarded ad from placement 'placementId'
//            }
//
//            override fun onAvailable(placementId: String) {
//                logAd("Rewarded ad loaded")
//                listener.onRewardedAdLoaded()
//            }
//
//            override fun onUnavailable(placementId: String) {
//                val adapterError = toMaxError(InneractiveErrorCode.NO_FILL)
//                logAd("Rewarded ad failed to load with Inneractive error: $adapterError")
//                listener.onRewardedAdLoadFailed(adapterError)
//            }
//
//            override fun onCompletion(placementId: String, userRewarded: Boolean) {
//                logAd("Rewarded video completed")
//                hasGrantedReward = userRewarded
//                listener.onUserRewarded(reward)
//            }
//
//            override fun onRequestStart(placementId: String, requestId: String) {
//                // Called when a rewarded ad from placement 'placementId' is going to be requested
//                // 'requestId' identifies the request across the whole request/show flow
//            }
//        })
//        Rewarded.request(rewardedUnit)
//
//    }
//
//    override fun showRewardedAd(
//        parameters: MaxAdapterResponseParameters,
//        activity: Activity,
//        listener: MaxRewardedAdapterListener
//    ) {
//        logAd("Showing rewarded ad...")
//        if (Rewarded.isAvailable(rewardedUnit)) {
//            // Configure userReward from server.
//            configureReward(parameters)
//            Rewarded.show(rewardedUnit, activity)
//        } else {
//            logAd("Rewarded ad not ready")
//            listener.onRewardedAdDisplayFailed(
//                MaxAdapterError(
//                    -4205,
//                    "Ad Display Failed",
//                    0,
//                    "Rewarded ad not ready"
//                )
//            )
//        }
//    }
//
//    override fun loadAdViewAd(
//        parameters: MaxAdapterResponseParameters,
//        adFormat: MaxAdFormat,
//        activity: Activity,
//        listener: MaxAdViewAdapterListener
//    ) {
//        logAd("Loading " + (if (AppLovinSdkUtils.isValidString(parameters.bidResponse)) "bidding " else "") + adFormat.label + " ad for spot id " + parameters.thirdPartyAdPlacementId + "\"...")
//        bannerUnit = parameters.thirdPartyAdPlacementId
//        updateUserInfo(parameters)
//        if (isBannerAdLoading) {
//            val adapterError = toMaxError(InneractiveErrorCode.CANCELLED)
//            listener.onAdViewAdLoadFailed(adapterError)
//            return
//        }
//        isBannerAdLoading = true
//        val scope = CoroutineScope(Dispatchers.Main)
//        val bannerSize = parameters.serverParameters.getBoolean("is_mrec")
//            .let { isMrec -> if (isMrec) BannerSize.MREC else BannerSize.SMART }
//        adViewGroup = RelativeLayout(getContext(activity))
//
//        Banner.setBannerListener(object : BannerListener {
//            override fun onError(placementId: String, error: BannerError) {
//                logAd("AdView failed to load with Inneractive error: ${error.errorMessage}")
//                listener.onAdViewAdLoadFailed(toMaxError(InneractiveErrorCode.NO_FILL))
//                runCatching {
//                    isBannerAdLoading = false
//                    scope.cancel()
//                }
//            }
//
//            override fun onLoad(placementId: String) {
//                logAd("AdView loaded")
//                listener.onAdViewAdLoaded(adViewGroup)
//                runCatching {
//                    isBannerAdLoading = false
//                    scope.cancel()
//                }
//            }
//
//            override fun onShow(
//                placementId: String,
//                impressionData: com.fyber.fairbid.ads.ImpressionData
//            ) {
//                logAd("AdView shown")
//
//                // Passing extra info such as creative id supported in 9.15.0+
//                val creativeId = impressionData.creativeId
//                if (!creativeId.isNullOrBlank()) {
//                    val extraInfo = Bundle(1)
//                    extraInfo.putString("creative_id", creativeId)
//                    listener.onAdViewAdDisplayed(extraInfo)
//                } else {
//                    listener.onAdViewAdDisplayed()
//                }
//            }
//
//            override fun onClick(placementId: String) {
//                logAd("AdView clicked")
//                listener.onAdViewAdClicked()
//            }
//
//            override fun onRequestStart(placementId: String, requestId: String) {
//                // Called when the banner from placement 'placementId' is going to be requested
//                // 'requestId' identifies the request across the whole request/show flow
//            }
//        })
//        Banner.show(
//            bannerUnit, BannerOptions()
//                .placeInContainer(adViewGroup!!)
//                .withSize(bannerSize), activity
//        )
//        scope.launchWithSupervisorJob {
//            delay(15000)
//            isBannerAdLoading = false
//        }
//    }
//
//    private fun updateUserInfo(parameters: MaxAdapterParameters) {
//        UserInfo.setUserId(wrappingSdk.userIdentifier)
//
//    }
//
//    private fun getContext(activity: Activity?): Context {
//        // NOTE: `activity` can only be null in 11.1.0+, and `getApplicationContext()` is introduced in 11.1.0
//        return if (activity != null) activity.applicationContext else applicationContext
//    }
//
//    companion object {
//        private val isInitializing = AtomicBoolean()
//        private var status: MaxAdapter.InitializationStatus? = null
//        private fun toMaxError(inneractiveErrorCode: InneractiveErrorCode?): MaxAdapterError {
//            var adapterError = MaxAdapterError.UNSPECIFIED
//            when (inneractiveErrorCode) {
//                InneractiveErrorCode.NO_FILL -> adapterError = MaxAdapterError.NO_FILL
//                InneractiveErrorCode.SERVER_INTERNAL_ERROR -> adapterError =
//                    MaxAdapterError.SERVER_ERROR
//
//                InneractiveErrorCode.SERVER_INVALID_RESPONSE -> adapterError =
//                    MaxAdapterError.BAD_REQUEST
//
//                InneractiveErrorCode.SDK_INTERNAL_ERROR, InneractiveErrorCode.ERROR_CODE_NATIVE_VIDEO_NOT_SUPPORTED, InneractiveErrorCode.NATIVE_ADS_NOT_SUPPORTED_FOR_OS, InneractiveErrorCode.UNSUPPORTED_SPOT, InneractiveErrorCode.NON_SECURE_CONTENT_DETECTED -> adapterError =
//                    MaxAdapterError.INTERNAL_ERROR
//
//                InneractiveErrorCode.CANCELLED -> adapterError = MaxAdapterError.AD_NOT_READY
//                InneractiveErrorCode.CONNECTION_TIMEOUT, InneractiveErrorCode.LOAD_TIMEOUT, InneractiveErrorCode.IN_FLIGHT_TIMEOUT -> adapterError =
//                    MaxAdapterError.TIMEOUT
//
//                InneractiveErrorCode.CONNECTION_ERROR -> adapterError =
//                    MaxAdapterError.NO_CONNECTION
//
//                InneractiveErrorCode.UNKNOWN_APP_ID, InneractiveErrorCode.INVALID_INPUT, InneractiveErrorCode.SDK_NOT_INITIALIZED, InneractiveErrorCode.SDK_NOT_INITIALIZED_OR_CONFIG_ERROR -> adapterError =
//                    MaxAdapterError.NOT_INITIALIZED
//
//                InneractiveErrorCode.ERROR_CONFIGURATION_MISMATCH, InneractiveErrorCode.ERROR_CONFIGURATION_NO_SUCH_SPOT, InneractiveErrorCode.SPOT_DISABLED -> adapterError =
//                    MaxAdapterError.INVALID_CONFIGURATION
//
//                InneractiveErrorCode.UNSPECIFIED -> adapterError = MaxAdapterError.UNSPECIFIED
//                null -> adapterError = MaxAdapterError.UNSPECIFIED
//                else -> {
//                    adapterError = MaxAdapterError.UNSPECIFIED
//                }
//            }
//            val adapterErrorCode: Int
//            val adapterErrorStr: String
//            if (inneractiveErrorCode != null) {
//                adapterErrorCode = inneractiveErrorCode.ordinal
//                adapterErrorStr = inneractiveErrorCode.name
//            } else {
//                adapterErrorCode = 0
//                adapterErrorStr = ""
//            }
//            return MaxAdapterError(
//                adapterError.errorCode,
//                adapterError.errorMessage,
//                adapterErrorCode,
//                adapterErrorStr
//            )
//        }
//    }
//}