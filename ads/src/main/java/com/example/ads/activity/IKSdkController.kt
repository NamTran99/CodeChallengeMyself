package com.example.ads.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.example.ads.activity.core.CoreController
import com.example.ads.activity.core.IKDataCoreManager
////import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.db.IkmSdkCacheFunc
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.pub.IKError
import com.example.ads.activity.data.dto.pub.IKRemoteConfigValue
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
//import com.example.ads.activity.format.banner.IKBannerController
//import com.example.ads.activity.format.banner.IKBannerInlineController
//import com.example.ads.activity.format.intertial.IKInterController
import com.example.ads.activity.format.native_ads.IKNativeController
import com.example.ads.activity.format.native_ads.IKNativeFullScreenController
//import com.example.ads.activity.format.open_ads.IKAppOpenController
//import com.example.ads.activity.format.rewarded.IKRewardedController
import com.example.ads.activity.listener.keep.IkSdkControllerInterface
import com.example.ads.activity.listener.keep.OnUserAttributionChangedListener
import com.example.ads.activity.listener.pub.IKAppOpenAdCallback
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKLoadDisplayAdViewListener
import com.example.ads.activity.listener.pub.IKNewRemoteConfigCallback
import com.example.ads.activity.listener.pub.IKNoneSplashAdListener
import com.example.ads.activity.listener.pub.IKRemoteConfigCallback
import com.example.ads.activity.listener.pub.IKShowAdListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.listener.sdk.IKSdkRemoteConfigCallback
//import com.example.ads.activity.mediation.playgap.IKPlayGapHelper
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IKTrackingConst
import com.example.ads.activity.utils.IKUtils
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
//import io.playgap.sdk.ClaimRewardError
//import io.playgap.sdk.ClaimRewardsListener
//import io.playgap.sdk.NetworkObserver
//import io.playgap.sdk.PlaygapAds
//import io.playgap.sdk.PlaygapReward
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

object IKSdkController : CoreController(), IkSdkControllerInterface {

    override fun setEnableShowResumeAds(enable: Boolean) {
        IKSdkOptions.setEnableShowResumeAds(enable)
    }

    override fun setEnableShowLoadingResumeAds(enable: Boolean) {
        IKSdkOptions.setEnableShowLoadingResumeAds(enable)
    }

    override fun addActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        IKSdkOptions.addActivityLifecycleCallbacks(callbacks)
    }

    override fun removeActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        IKSdkOptions.removeActivityLifecycleCallbacks(callbacks)
    }

    override fun clearActivityLifecycleCallbacks() {
        IKSdkOptions.clearActivityLifecycleCallbacks()
    }

    override fun setOnAttributionChangedListener(value: OnUserAttributionChangedListener) {
        IKSdkOptions.setOnAttributionChangedListener(value)
    }

    override fun addOnNetworkConnectivityCallback(callback: ConnectivityManager.NetworkCallback) {
        IKSdkOptions.addOnNetworkConnectivityCallback(callback)
    }

    override fun removeOnNetworkConnectivityCallback(callback: ConnectivityManager.NetworkCallback) {
        IKSdkOptions.removeOnNetworkConnectivityCallback(callback)
    }

    override fun clearOnNetworkConnectivityCallback() {
        IKSdkOptions.clearOnNetworkConnectivityCallback()
    }

    override fun reloadNetworkState(context: Context) {
        IKSdkOptions.reloadNetworkState(context)
    }

    override fun setAppOpenAdsCallback(callback: IKAppOpenAdCallback) {
        IKSdkOptions.setAppOpenAdsCallback(callback)
    }

    override fun removeAppOpenAdsCallback() {
        IKSdkOptions.removeAppOpenAdsCallback()
    }

    override fun getAppOpenAdsCallback(): IKAppOpenAdCallback? {
        return IKSdkOptions.getAppOpenAdsCallback()
    }

    override fun addActivityEnableShowResumeAd(vararg activity: Class<*>) {
        IKSdkOptions.addActivityEnableShowResumeAd(*activity)
    }

    override fun removeActivityEnableShowResumeAd(vararg activity: Class<*>) {
        IKSdkOptions.removeActivityEnableShowResumeAd(*activity)
    }

    override fun clearActivityEnableShowResumeAd() {
        IKSdkOptions.clearActivityEnableShowResumeAd()
    }

    override fun pxc(activity: Activity?, callback: (() -> Unit)) {
        IKSdkOptions.pxc(activity, callback)
    }

    suspend fun requestCMPForm(activity: Activity?): Boolean {
        showLogSdk("requestCMPForm") { "cmp start run" }
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        val resultDeferred = CompletableDeferred<Boolean>()
        var callbackFunction: ((isConfirm: Boolean) -> Unit)? = {
            resultDeferred.complete(it)
        }
        var isFormChecked = false
//        if (activity != null) {
//            IKSdkTrackingHelper.customizeTracking(
//                IKTrackingConst.EventName.CMP_TRACK,
//                Pair(IKTrackingConst.ParamName.ACTION, "check")
//            )
//            val consentInformation =
//                UserMessagingPlatform.getConsentInformation(activity.applicationContext)
//            consentInformation.requestConsentInfoUpdate(
//                activity,
//                params,
//                {
//                    showLogSdk("requestCMPForm") { "cmp checkForm onUpdate" }
//                    IKSdkTrackingHelper.customizeTracking(
//                        IKTrackingConst.EventName.CMP_TRACK,
//                        Pair(IKTrackingConst.ParamName.ACTION, "needed")
//                    )
//                    isFormChecked = true
//                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
//                        if (consentInformation.canRequestAds()) {
//                            showLogSdk("requestCMPForm") { "cmp check showForm canLoadAd" }
//                            IKSdkTrackingHelper.customizeTracking(
//                                IKTrackingConst.EventName.CMP_TRACK,
//                                Pair(IKTrackingConst.ParamName.ACTION, "confirmed")
//                            )
//                            callbackFunction?.invoke(true)
//                            callbackFunction = null
//                            initializeMobileAdsSdk()
//                        } else {
//                            showLogSdk("requestCMPForm") { "cmp check showForm disable" }
//                            IKSdkTrackingHelper.customizeTracking(
//                                IKTrackingConst.EventName.CMP_TRACK,
//                                Pair(IKTrackingConst.ParamName.ACTION, "confirm_fail")
//                            )
//                            callbackFunction?.invoke(false)
//                            callbackFunction = null
//                        }
//                    }
//                },
//                {
//                    showLogSdk("requestCMPForm") { "cmp check onFail" }
//                    IKSdkTrackingHelper.customizeTracking(
//                        IKTrackingConst.EventName.CMP_TRACK,
//                        Pair(IKTrackingConst.ParamName.ACTION, "no_need")
//                    )
//                    isFormChecked = true
//                    callbackFunction?.invoke(true)
//                    callbackFunction = null
//                }
//            )
//        } else {
//            callbackFunction?.invoke(false)
//            callbackFunction = null
//            return resultDeferred.await()
//        }

        mUiScope.launchWithSupervisorJob {
            delay(4000)
            showLogSdk("requestCMPForm") { "cmp check timeout isFormShowed:$isFormChecked" }
            if (!isFormChecked) {
                callbackFunction?.invoke(false)
                callbackFunction = null
            }
        }

        return resultDeferred.await()
    }

    override fun noneShowSplashAd(activity: Activity?, listener: IKNoneSplashAdListener) {
        val weakReference = WeakReference(activity)
        val context = weakReference.get()?.applicationContext
        val act = weakReference.get()
        showLogSdk("noneShowSplashAd") { "start run" }
        splashInit()
        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            val canRequestAd = IKSdkDataStore.getBoolean(IKSdkDataStoreConst.KEY_CMP_STATUS, false)
            val checkCountry = IKSdkUtilsCore.checkCmpCountryCode(context)
            val needRequest =
                IKSdkDataStore.getBoolean(
                    IKSdkDataStoreConst.CMP_CONFIG_REQUEST_ENABLE,
                    false
                ) || checkCountry
            showLogSdk("noneShowSplashAd") { "cmp start needRequest=$needRequest,canRequestAd=$canRequestAd" }
            if (!needRequest) {
                mUiScope.launchWithSupervisorJob {
                    listener.onMove()
                }
                showLogSdk("noneShowSplashAd") { "cmp not need request" }
                return@launchWithSupervisorJob
            }
            if (!IKSdkUtilsCore.isConnectionAvailableAsync()) {
                mUiScope.launchWithSupervisorJob {
                    listener.onMove()
                }
                showLogSdk("noneShowSplashAd") { "cmp not connect internet" }
                return@launchWithSupervisorJob
            }
            if (canRequestAd) {
                mUiScope.launchWithSupervisorJob {
                    listener.onMove()
                }
                val params = ConsentRequestParameters
                    .Builder()
                    .setTagForUnderAgeOfConsent(false)
                    .build()
                showLogSdk("noneShowSplashAd") { "cmp start request form" }
                if (act != null) {
                    val consentInformation =
                        UserMessagingPlatform.getConsentInformation(act.applicationContext)
                    consentInformation.requestConsentInfoUpdate(
                        act,
                        params,
                        {
                            showLogSdk("noneShowSplashAd") { "cmp recheck onUpdate" }
                            this.launchWithSupervisorJob {
                                if (consentInformation.canRequestAds())
                                    IKSdkDataStore.putBoolean(
                                        IKSdkDataStoreConst.KEY_CMP_STATUS,
                                        true
                                    )
                                else IKSdkDataStore.putBoolean(
                                    IKSdkDataStoreConst.KEY_CMP_STATUS,
                                    false
                                )
                            }
                        },
                        {
                            showLogSdk("noneShowSplashAd") { "cmp  recheck onFail" }
                        })
                }
                return@launchWithSupervisorJob
            }

            requestCMPForm(act)
            mUiScope.launchWithSupervisorJob {
                listener.onMove()
            }
        }
    }

    override suspend fun isRewardAdReadyAsync(): Boolean {
        return false
//        return IKRewardedController.isAdReady()
    }

    override fun isRewardAdReadyAsync(callback: (value: Boolean) -> Unit) {
        mUiScope.launchWithSupervisorJob {
//            callback.invoke(IKRewardedController.isAdReady())
        }
    }

    override suspend fun isInterAdReadyAsync(): Boolean {
        return false
//        return IKInterController.isAdReady()
    }

    override fun isInterAdReadyAsync(callback: (value: Boolean) -> Unit) {
        mUiScope.launchWithSupervisorJob {
//            callback.invoke(IKInterController.isAdReady())
        }
    }

    override suspend fun isAppOpenAdReadyAsync(): Boolean {
        return false
//        return IKAppOpenController.isAdReady()
    }

    override fun isAppOpenAdReadyAsync(callback: (value: Boolean) -> Unit) {
        mUiScope.launchWithSupervisorJob {
//            callback.invoke(IKAppOpenController.isAdReady())
        }
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isRewardAdReadyAsync instead.",
        replaceWith = ReplaceWith("isAppOpenAdReadyAsync()"),
        level = DeprecationLevel.WARNING
    )
    override fun isAppOpenAdReady(): Boolean {
        var isReady: Boolean
        runBlocking(Dispatchers.Default) {
//            isReady = IKAppOpenController.isAdReady()
        }
//        return isReady
        return false
    }

    override suspend fun isNativeAdReadyAsync(): Boolean {
        return IKNativeController.isAdReady()
    }

    override fun isNativeAdReadyAsync(callback: (value: Boolean) -> Unit) {
        mUiScope.launchWithSupervisorJob {
            callback.invoke(IKNativeController.isAdReady())
        }
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isRewardAdReadyAsync instead.",
        replaceWith = ReplaceWith("isNativeAdReadyAsync()"),
        level = DeprecationLevel.WARNING
    )
    override fun isNativeAdReady(): Boolean {
        var isReady: Boolean
        runBlocking(Dispatchers.Default) {
            isReady = IKNativeController.isAdReady()
        }
        return isReady
    }

    override suspend fun isBannerAdReadyAsync(): Boolean {
        return false
//        return IKBannerController.isAdReady()
    }

    override fun isBannerAdReadyAsync(callback: (value: Boolean) -> Unit) {
        mUiScope.launchWithSupervisorJob {
//            callback.invoke(IKBannerController.isAdReady())
        }
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isRewardAdReadyAsync instead.",
        replaceWith = ReplaceWith("isBannerAdReadyAsync()"),
        level = DeprecationLevel.WARNING
    )
    override fun isBannerAdReady(): Boolean {
        var isReady: Boolean = false
        runBlocking(Dispatchers.Default) {
//            isReady = IKBannerController.isAdReady()
        }
        return isReady
    }

    override suspend fun isBannerInlineAdReady(): Boolean {
        return false
//        return IKBannerInlineController.isAdReady()
    }


    override fun setEnableTimeOutOpenAd(enable: Boolean, timeOut: Long) {
        IKSdkOptions.mEnableTimeOutShowOpenAd = enable
        if (timeOut > 5000)
            IKSdkOptions.mTimeOutShowOpenAd = timeOut
    }

    override fun setEnableTimeOutInterAd(enable: Boolean, timeOut: Long) {
        IKSdkOptions.mEnableTimeOutShowInterstitialAd = enable
        if (timeOut > 10000)
            IKSdkOptions.mTimeOutShowInterstitialAd = timeOut
    }

    override suspend fun isScreenNameAvailable(adsType: IKAdFormat, screenName: String): Boolean {
        showLogSdk("isScreenNameAvailable") { "screenName=$screenName adsType=$adsType" }
        return withContext(Dispatchers.Default) {
            return@withContext when (adsType) {
                IKAdFormat.INTER -> {
                    mRepository?.getProductConfigInter(screenName) != null
                }

                IKAdFormat.NATIVE -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.NATIVE) != null
                }

                IKAdFormat.NATIVE_BANNER -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.NATIVE_BANNER) != null
                }

                IKAdFormat.BANNER -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.BANNER) != null
                }

                IKAdFormat.REWARD -> {
                    mRepository?.getConfigReward(screenName) != null
                }

                IKAdFormat.BANNER_INLINE -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.BANNER_INLINE) != null
                }

                IKAdFormat.MREC -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.MREC) != null
                }

                IKAdFormat.NATIVE_FULL -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.NATIVE_FULL) != null
                }

                IKAdFormat.BANNER_COLLAPSE -> {
                    mRepository?.getConfigWidget(screenName, IKAdFormat.BANNER_COLLAPSE) != null
                }

                else -> {
                    false
                }
            }
        }
    }

    override suspend fun isScreenNameAvailable(screenName: String): Boolean {
        showLogSdk("isScreenNameAvailable") { "screenName=$screenName" }
        return withContext(Dispatchers.Default) {
            when {
                mRepository?.getProductConfigInter(screenName) != null -> true
                mRepository?.getConfigReward(screenName) != null -> true
                mRepository?.getConfigWidget(screenName) != null -> true
                else -> false
            }
        }
    }

    override fun preloadNativeAd(
        screen: String,
        callback: IKLoadAdListener?
    ) {
        showLogSdk("preloadNativeAd") { "screen = $screen start run" }
        IKNativeController.preloadAd(screen, object : IKSdkLoadAdCoreListener {
            override fun onAdLoaded() {
                callback?.onAdLoaded()
                showLogSdk("preloadNativeAd") { "screen = $screen onAdLoaded" }
            }

            override fun onAdLoadFail(error: IKAdError) {
                callback?.onAdLoadFail(error)
                showLogSdk("preloadNativeAd") { "screen = $screen error = $error" }
            }
        })
    }

    override fun preloadNativeAdFullScreen(
        screen: String,
        callback: IKLoadAdListener?
    ) {
        showLogSdk("preloadNativeAdFullScreen") { "screen = $screen start run" }
        IKNativeFullScreenController.preloadAd(screen, object : IKSdkLoadAdCoreListener {
            override fun onAdLoaded() {
                callback?.onAdLoaded()
                showLogSdk("preloadNativeAdFullScreen") { "screen = $screen onAdLoaded" }
            }

            override fun onAdLoadFail(error: IKAdError) {
                callback?.onAdLoadFail(error)
                showLogSdk("preloadNativeAdFullScreen") { "screen = $screen error = $error" }
            }
        })
    }

    override fun preloadBannerAd(
        screen: String,
        callback: IKLoadAdListener?
    ) {
        showLogSdk("preloadBannerAd") { "screen = $screen start run" }
//        IKBannerController.preloadAd(screen, object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                callback?.onAdLoaded()
//                showLogSdk("preloadBannerAd") { "screen = $screen onAdLoaded" }
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                callback?.onAdLoadFail(error)
//                showLogSdk("preloadBannerAd") { "screen = $screen error = $error" }
//            }
//        })
    }

    override fun preloadBnBAd(
        screen: String,
        callback: IKLoadAdListener?
    ) {
        showLogSdk("preloadBnBAd") { "screen = $screen start run" }
//        IKBannerInlineController.preloadAd(screen, object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                callback?.onAdLoaded()
//                showLogSdk("preloadBannerAd") { "screen = $screen onAdLoaded" }
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                callback?.onAdLoadFail(error)
//                showLogSdk("preloadBannerAd") { "screen = $screen error = $error" }
//            }
//        })
    }

    override fun loadNativeDisplayAd(
        screen: String,
        callback: IKLoadDisplayAdViewListener?
    ) {
        showLogSdk("loadNativeDisplayAd") { "screen = $screen start run" }
        IKNativeController.loadDisplayAd(screen, object : IKLoadDisplayAdViewListener {
            override fun onAdLoaded(adObject: IkmDisplayWidgetAdView?) {
                callback?.onAdLoaded(adObject)
                showLogSdk("onAdLoaded") { "screen = $screen onAdLoaded" }
            }

            override fun onAdLoadFail(error: IKAdError) {
                callback?.onAdLoadFail(error)
                showLogSdk("onAdLoadFail") { "screen = $screen error = $error" }
            }
        })
    }

    override suspend fun getNativeDisplayAdAsync(): IkmDisplayWidgetAdView? {
        return IKNativeController.getDisplayAd()
    }

    override fun setOnRemoteConfigDataListener(callback: IKRemoteConfigCallback?) {
//        IKRemoteDataManager.configCallback = object : IKSdkRemoteConfigCallback {
//            override fun onSuccess() {
//                mUiScope.launchWithSupervisorJob {
//                    callback?.onSuccess(IKRemoteDataManager.getRemoteConfigDataProduct())
//                }
//            }
//
//            override fun onFail(error: IKError?) {
//                callback?.onFail()
//            }
//        }
    }

    override fun setOnRemoteConfigDataListener(callback: IKNewRemoteConfigCallback?) {
//        IKRemoteDataManager.configCallback = object : IKSdkRemoteConfigCallback {
//            override fun onSuccess() {
//                mUiScope.launchWithSupervisorJob {
//                    callback?.onSuccess(IKRemoteDataManager.getRemoteConfigDataProduct())
//                }
//            }
//
//            override fun onFail(error: IKError?) {
//                callback?.onFail(error)
//            }
//        }
    }

    override fun removeOnRemoteConfigDataListener() {
//        IKRemoteDataManager.configCallback = null
    }

    override fun getOtherConfigData(): HashMap<String, Any> {
        return IKDataCoreManager.otherConfig
    }

    override suspend fun isAnOtherAdsShowing(): Boolean {
        return super.isAnOtherAdsShowing()
    }

    override fun loadAppOpenAd(callback: IKLoadAdListener?) {
        showLogSdk("loadAppOpenAd") { "start run" }
//        IKAppOpenController.loadAdBase(
//            IKSdkDefConst.AdScreen.IN_APP,
//            object : IKSdkLoadAdCoreListener {
//                override fun onAdLoaded() {
//                    callback?.onAdLoaded()
//                    showLogSdk("loadAppOpenAd") { "onAdLoaded" }
//                }
//
//                override fun onAdLoadFail(error: IKAdError) {
//                    callback?.onAdLoadFail(error)
//                    showLogSdk("loadAppOpenAd") { "onAdLoadFail error=$error" }
//                }
//            })
    }


    override fun handleShowClaimAd(context: Activity?, coroutineScope: CoroutineScope?) {
        if (context == null) {
            return
        }
//        if (!IKPlayGapHelper.hasLib()) {
//            showLogSdk("showClaimConfirmAd") {
//                "No lib"
//            }
//            return
//        }
//        if (!IKInterController.canAutoShowClaimAdPlayGap())
//            return
        val scope = coroutineScope ?: mUiScope
        var lastSentTime = 0L
        val networkChannel = Channel<Boolean>(Channel.BUFFERED)
//        PlaygapAds.registerNetworkObserver(
//            context = context,
//            observer = object : NetworkObserver {
//                override fun onNetworkChange(isConnected: Boolean) {
//                    showLogSdk("handleShowClaimAd") {
//                        "onNetworkChange:$isConnected"
//                    }
//                    if (!IKInterController.canAutoShowClaimAdPlayGap())
//                        return
//                    scope.launchWithSupervisorJob {
//                        val currentTime = System.currentTimeMillis()
//                        if (isConnected && currentTime - lastSentTime >= 30000) {
//                            lastSentTime = currentTime
//                            networkChannel.send(true)
//                        }
//                    }
//                }
//            }
//        )
        scope.launch {
            for (connected in networkChannel) {
                if (connected) {
                    showClaimConfirmAdAsync(context, null)
                }
            }
        }

    }

    override fun showClaimConfirmAd(
        context: Activity?,
        callback: IKShowAdListener?,
        coroutineScope: CoroutineScope?
    ) {
        val scope = coroutineScope ?: mUiScope

        scope.launch {
            showClaimConfirmAd(context, callback)
        }
    }

    override fun showClaimConfirmAd(
        context: Activity?,
        callback: IKShowAdListener?
    ) {
        mUiScope.launch {
            showClaimConfirmAd(context, callback)
        }
    }

    override suspend fun showClaimConfirmAdAsync(activity: Activity?, callback: IKShowAdListener?) {
        showLogSdk("showClaimConfirmAd") {
            "start"
        }
//        if (!IKPlayGapHelper.hasLib()) {
//            showLogSdk("showClaimConfirmAd") {
//                "No lib"
//            }
//            callback?.onAdsShowFail(IKAdError(0, "Context null"))
//            return
//        }
//        if (activity == null) {
//            showLogSdk("showClaimConfirmAd") {
//                "Context null"
//            }
//            callback?.onAdsShowFail(IKAdError(0, "Context null"))
//            return
//        }

        if (!IKUtils.isConnectionAvailable() || isAnOtherAdsShowing()) {
            showLogSdk("showClaimConfirmAd") {
                "connect fail"
            }
            callback?.onAdsShowFail(IKAdError(0, "connect fail"))
            return
        }
//        val rewards = PlaygapAds.checkRewards()

//        if (rewards?.unclaimed.isNullOrEmpty()) {
//            showLogSdk("showClaimConfirmAd") {
//                "No data"
//            }
//            callback?.onAdsShowFail(IKAdError(0, "No data"))
//            return
//        }
//        PlaygapAds.claimRewards(
//            activity,
//            listener = object : ClaimRewardsListener {
//                override fun onRewardScreenShown() {
//                    callback?.onAdsShowed()
//                    showLogSdk("showClaimConfirmAd") {
//                        "onRewardScreenShown"
//                    }
//                }
//
//                override fun onUserClaimedRewards(rewards: List<PlaygapReward>) {
//                    showLogSdk("showClaimConfirmAd") {
//                        "onUserClaimedReward"
//                    }
//                }
//
//                override fun onRewardScreenFailed(error: ClaimRewardError) {
//                    callback?.onAdsShowFail(IKAdError(0, "${error.message}"))
//                    showLogSdk("showClaimConfirmAd") {
//                        "onRewardScreenFailed"
//                    }
//                }
//
//                override fun onRewardScreenClosed() {
//                    callback?.onAdsDismiss()
//                    showLogSdk("showClaimConfirmAd") {
//                        "onRewardScreenClosed"
//                    }
//                }
//            }
//        )
    }

    override fun setUserProperty(key: String, value: String) {
        mUiScope.launchWithSupervisorJob {
            IKSdkDataStore.putString(IKSdkDefConst.Config.SDK_USER_PROPERTY_TYPE, key + "_" + value)
            fetchNewRemoteConfigData(object : IKRemoteConfigCallback {
                override fun onSuccess(data: HashMap<String, IKRemoteConfigValue>) {
                }

                override fun onFail() {
                }

            })
        }
    }

    override suspend fun getRemoteConfigData(): java.util.HashMap<String, IKRemoteConfigValue> {
        return  hashMapOf()
//        return IKRemoteDataManager.getRemoteConfigDataProduct()
    }

    override suspend fun getRemoteConfigDataAsync(): java.util.HashMap<String, IKRemoteConfigValue> {
//        return withContext(Dispatchers.Default) {
//            IKRemoteDataManager.getRemoteConfigDataProduct()
//        }
        return  hashMapOf()
    }

    override fun splashInit() {
//        IKRemoteDataManager.checkUpdateRemoteConfig()
        IkmSdkCacheFunc.AD.resetInterAdFrequency()
    }

    override fun fetchNewRemoteConfigData(callback: IKRemoteConfigCallback) {
//        IKRemoteDataManager.fetchNewRemoteConfigData(object : IKNewRemoteConfigCallback {
//            override fun onSuccess(data: java.util.HashMap<String, IKRemoteConfigValue>) {
//                callback.onSuccess(data)
//            }
//
//            override fun onFail(error: IKError?) {
//                callback.onFail()
//            }
//
//        })
    }

    override fun fetchNewRemoteConfigData(callback: IKNewRemoteConfigCallback) {
//        IKRemoteDataManager.fetchNewRemoteConfigData(object : IKNewRemoteConfigCallback {
//            override fun onSuccess(data: java.util.HashMap<String, IKRemoteConfigValue>) {
//                callback.onSuccess(data)
//            }
//
//            override fun onFail(error: IKError?) {
//                callback.onFail(error)
//            }
//
//        })
    }
}