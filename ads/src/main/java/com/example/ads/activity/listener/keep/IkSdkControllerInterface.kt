package com.example.ads.activity.listener.keep

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.pub.IKRemoteConfigValue
import com.example.ads.activity.listener.pub.IKAppOpenAdCallback
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKLoadDisplayAdViewListener
import com.example.ads.activity.listener.pub.IKNewRemoteConfigCallback
import com.example.ads.activity.listener.pub.IKNoneSplashAdListener
import com.example.ads.activity.listener.pub.IKRemoteConfigCallback
import com.example.ads.activity.listener.pub.IKShowAdListener
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import kotlinx.coroutines.CoroutineScope

interface IkSdkControllerInterface {
    fun setUserProperty(key: String, value: String)
    suspend fun getRemoteConfigData(): HashMap<String, IKRemoteConfigValue>
    suspend fun getRemoteConfigDataAsync(): HashMap<String, IKRemoteConfigValue>
    fun splashInit()
    fun noneShowSplashAd(activity: Activity?, listener: IKNoneSplashAdListener)
    suspend fun showClaimConfirmAdAsync(activity: Activity?, callback: IKShowAdListener? = null)
    fun showClaimConfirmAd(
        context: Activity?,
        callback: IKShowAdListener? = null
    )

    fun showClaimConfirmAd(
        context: Activity?,
        callback: IKShowAdListener? = null,
        coroutineScope: CoroutineScope? = null
    )

    fun handleShowClaimAd(context: Activity?, coroutineScope: CoroutineScope?)
    fun loadAppOpenAd(callback: IKLoadAdListener?)
    fun getOtherConfigData(): HashMap<String, Any>
    fun removeOnRemoteConfigDataListener()
    fun setOnRemoteConfigDataListener(callback: IKNewRemoteConfigCallback?)
    fun setOnRemoteConfigDataListener(callback: IKRemoteConfigCallback?)
    suspend fun getNativeDisplayAdAsync(): IkmDisplayWidgetAdView?
    fun loadNativeDisplayAd(
        screen: String,
        callback: IKLoadDisplayAdViewListener?
    )

    fun preloadBnBAd(
        screen: String,
        callback: IKLoadAdListener?
    )

    fun preloadBannerAd(
        screen: String,
        callback: IKLoadAdListener?
    )

    fun preloadNativeAdFullScreen(
        screen: String,
        callback: IKLoadAdListener?
    )

    fun preloadNativeAd(
        screen: String,
        callback: IKLoadAdListener?
    )

    suspend fun isScreenNameAvailable(adsType: IKAdFormat, screenName: String): Boolean
    suspend fun isScreenNameAvailable(screenName: String): Boolean
    fun pxc(activity: Activity?, callback: (() -> Unit))
    fun clearActivityEnableShowResumeAd()
    fun removeActivityEnableShowResumeAd(vararg activity: Class<*>)
    fun addActivityEnableShowResumeAd(vararg activity: Class<*>)
    fun getAppOpenAdsCallback(): IKAppOpenAdCallback?
    fun removeAppOpenAdsCallback()
    fun setAppOpenAdsCallback(callback: IKAppOpenAdCallback)
    fun reloadNetworkState(context: Context)
    fun clearOnNetworkConnectivityCallback()
    fun removeOnNetworkConnectivityCallback(callback: ConnectivityManager.NetworkCallback)
    fun addOnNetworkConnectivityCallback(callback: ConnectivityManager.NetworkCallback)
    fun setEnableShowResumeAds(enable: Boolean)
    fun setEnableShowLoadingResumeAds(enable: Boolean)
    fun addActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks)
    fun removeActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks)
    fun clearActivityLifecycleCallbacks()
    fun setOnAttributionChangedListener(value: OnUserAttributionChangedListener)

    suspend fun isRewardAdReadyAsync(): Boolean
    fun isRewardAdReadyAsync(callback: (value: Boolean) -> Unit)

    suspend fun isInterAdReadyAsync(): Boolean
    fun isInterAdReadyAsync(callback: (value: Boolean) -> Unit)

    suspend fun isAppOpenAdReadyAsync(): Boolean

    fun isAppOpenAdReadyAsync(callback: (value: Boolean) -> Unit)

    fun isAppOpenAdReady(): Boolean

    suspend fun isNativeAdReadyAsync(): Boolean

    fun isNativeAdReadyAsync(callback: (value: Boolean) -> Unit)

    fun isNativeAdReady(): Boolean

    suspend fun isBannerAdReadyAsync(): Boolean

    fun isBannerAdReadyAsync(callback: (value: Boolean) -> Unit)

    fun isBannerAdReady(): Boolean

    suspend fun isBannerInlineAdReady(): Boolean

    fun setEnableTimeOutOpenAd(enable: Boolean, timeOut: Long = 25000)

    fun setEnableTimeOutInterAd(enable: Boolean, timeOut: Long = 30000)
    fun fetchNewRemoteConfigData(callback: IKRemoteConfigCallback)
    fun fetchNewRemoteConfigData(callback: IKNewRemoteConfigCallback)
}