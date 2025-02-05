package com.example.ads.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.pub.SDKNetworkType
import com.example.ads.activity.listener.keep.OnUserAttributionChangedListener
import com.example.ads.activity.listener.pub.IKAppOpenAdCallback
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IkmSdkCoreFunc

object IKSdkOptions {
    var delayHandlerShowResumeAds = 200L
        set(value) {
            field = if (value < 200)
                200
            else
                value
        }

    var delayTimeShowShowResumeAds = 0L
        set(value) {
            field = if (value < 0)
                0
            else
                value
        }

    var mEnableShowResumeAds = false
        private set

    fun setEnableShowResumeAds(enable: Boolean) {
        mEnableShowResumeAds = enable
    }

    var mEnableShowLoadingResumeAds = false
        private set

    var mEnableTimeOutShowOpenAd = false
    var mTimeOutShowOpenAd = 30000L
    var mEnableTimeOutShowInterstitialAd = false
    var mTimeOutShowInterstitialAd = 30000L
    var enablePreloadInterAd = true
    var enablePreloadReward = false
    var openAdResumeTrackingScreen: String = IKSdkDefConst.AdScreen.IN_APP

    fun setEnableShowLoadingResumeAds(enable: Boolean) {
        mEnableShowLoadingResumeAds = enable
    }

    fun addActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        IkmSdkCoreFunc.AppF.mListActLifecycleCallbacks.add(callbacks)
    }

    fun removeActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        IkmSdkCoreFunc.AppF.mListActLifecycleCallbacks.remove(callbacks)
    }

    fun clearActivityLifecycleCallbacks() {
        IkmSdkCoreFunc.AppF.mListActLifecycleCallbacks.clear()
    }

    fun setOnAttributionChangedListener(var1: OnUserAttributionChangedListener) {
        IkmSdkCoreFunc.AppF.mAttributionChangedListener = var1
    }


    fun addOnNetworkConnectivityCallback(callback: ConnectivityManager.NetworkCallback) {
        IkmSdkCoreFunc.AppF.mCustomOnNetworkConnectivityCallback.add(callback)
    }

    fun removeOnNetworkConnectivityCallback(callback: ConnectivityManager.NetworkCallback) {
        IkmSdkCoreFunc.AppF.mCustomOnNetworkConnectivityCallback.remove(callback)
    }

    fun clearOnNetworkConnectivityCallback() {
        IkmSdkCoreFunc.AppF.mCustomOnNetworkConnectivityCallback.clear()
    }

    fun setEnableRewarded(value: Boolean) {

    }

    fun reloadNetworkState(context: Context) {
        IkmSdkCoreFunc.AppF.mSDKNetworkType = IKSdkUtilsCore.isInternetAvailable(context)
        IkmSdkCoreFunc.AppF.isInternetAvailable =
            IkmSdkCoreFunc.AppF.mSDKNetworkType != SDKNetworkType.NotConnect
    }

    fun setAppOpenAdsCallback(callback: IKAppOpenAdCallback) {
        IkmSdkCoreFunc.AppF.mIKAppOpenAdCallback = callback
    }

    fun getAppOpenAdsCallback(): IKAppOpenAdCallback? {
        return IkmSdkCoreFunc.AppF.mIKAppOpenAdCallback
    }

    fun removeAppOpenAdsCallback() {
        IkmSdkCoreFunc.AppF.mIKAppOpenAdCallback = null
    }

    fun addActivityEnableShowResumeAd(vararg activity: Class<*>) {
        IkmSdkCoreFunc.AppF.mActivityEnableShowResumeAd.addAll(activity)
    }

    fun removeActivityEnableShowResumeAd(vararg activity: Class<*>) {
        IkmSdkCoreFunc.AppF.mActivityEnableShowResumeAd.removeAll(activity.toSet())
    }

    fun clearActivityEnableShowResumeAd() {
        IkmSdkCoreFunc.AppF.mActivityEnableShowResumeAd.clear()
    }

    fun pxc(activity: Activity?, callback: (() -> Unit)) {
        var cxx: (() -> Unit)? = callback
        val xx = SDKDataHolder.FFun.umFdXC(activity) {
            cxx?.invoke()
            cxx = null
        }
        if (!xx) {
            cxx?.invoke()
            cxx = null
        }
    }
}