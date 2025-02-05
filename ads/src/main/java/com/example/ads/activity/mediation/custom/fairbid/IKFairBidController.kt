//package com.example.ads.activity.mediation.custom.fairbid
//
//import android.app.Activity
//import com.example.ads.activity.data.dto.sdk.IKCustomEventData
//import com.example.ads.activity.listener.sdk.IKSdkCustomEventAdListener
//import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class IKFairBidController {
//    private var listUnitInter: MutableList<IKCustomEventData> = mutableListOf()
//    private var isInterAdLoading = false
//    private val scope = CoroutineScope(Dispatchers.Main)
//    private val mInterstitial: IKFairBidInterstitial by lazy {
//        IKFairBidInterstitial()
//    }
//
//    fun loadInterstitialAd(
//        data: List<IKCustomEventData>,
//        listener: IKSdkCustomEventAdListener
//    ) {
//        scope.launchWithSupervisorJob {
//            mInterstitial.loadAd(data, listener)
//        }
//    }
//
//    fun showAd(activity: Activity, listener: IKSdkShowAdListener) {
//        scope.launchWithSupervisorJob {
//            mInterstitial.showAd(activity, listener)
//        }
//    }
//
//    suspend fun isInterAdReady(): Boolean {
//        return mInterstitial.isAdReady()
//    }
//
//
//    fun destroy() {
//        mInterstitial.destroy()
//    }
//
//}