//package com.example.ads.activity.mediation.playgap
//
//import android.app.Activity
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKCustomEventData
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
//import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
//import com.example.ads.activity.format.intertial.IKSdkBaseInterstitialAds
//import com.example.ads.activity.listener.sdk.IKSdkAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkBaseListener
//import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import io.playgap.sdk.PlaybackEvent
//import io.playgap.sdk.PlaygapAds
//import io.playgap.sdk.PlaygapReward
//import io.playgap.sdk.ShowError
//import io.playgap.sdk.ShowListener
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//
//class InterstitialPlayGap : IKSdkBaseInterstitialAds<IKCustomEventData>(AdNetwork.PLAYGAP) {
//    override fun showAd(
//        coroutineScope: CoroutineScope,
//        activity: Activity,
//        screen: String,
//        scriptName: String,
//        adsListener: IKSdkBaseListener
//    ) {
//        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
//            isAdShowing = true
//            PlaygapAds.showInterstitial(
//                 activity,
//                listener = object : ShowListener {
//                    override fun onShowFailed(error: ShowError) {
//                        isAdShowing = false
//                        adsListener.onAdShowFailed(
//                            adNetworkName,
//                            screen,
//                            IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + 0,
//                            IKAdError(IKSdkErrorCode.SHOWING_FAIL)
//                        )
//                        showLogD("showAd onAdDisplayed error ")
//                    }
//
//                    override fun onShowImpression(impressionId: String) {
//                        showLogD("onAdDisplayed")
//                        adsListener.onAdShowed(
//                            adNetworkName,
//                            screen,
//                            IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + 0,
//                             0,  ""
//                        )
//
//                        showLogD("showAdOnAdImpression")
//                        adsListener.onAdImpression(
//                            adNetworkName,
//                            screen,
//                            IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + 0,
//                            ""
//                        )
//
//                    }
//
//                    override fun onShowPlaybackEvent(event: PlaybackEvent) {
//
//                    }
//
//                    override fun onShowCompleted() {
//                        isAdShowing = false
//                        showLogD("onAdHidden")
//                        adsListener.onAdDismissed(
//                            adNetworkName,
//                            screen,
//                            IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + 0,
//                            ""
//                        )
//                    }
//
//                    override fun onUserEarnedReward(reward: PlaygapReward) {
//
//                    }
//                }
//            )
//
//            showLogD("showAd start show")
//
//        }
//    }
//
//    override suspend fun loadCoreAd(
//        coroutineScope: CoroutineScope,
//        idAds: IKAdUnitDto,
//        scriptName: String,
//        screen: String?,
//        showPriority: Int,
//        isLoadAndShow: Boolean,
//        callback: IKSdkAdCallback<IKCustomEventData>
//    ) {
//
//    }
//
//    override fun loadAd(
//        coroutineScope: CoroutineScope,
//        adData: IKAdapterDto?,
//        callback: IKSdkLoadAdCoreListener?
//    ) {
//        if (IKPlayGapHelper.initStatus)
//            super.loadAd(coroutineScope, adData, callback)
//        else {
//            coroutineScope.launchWithSupervisorJob {
//                delay(2000)
//                if (IKPlayGapHelper.initStatus)
//                    super.loadAd(coroutineScope, adData, callback)
//                else delay(1200)
//                super.loadAd(coroutineScope, adData, callback)
//            }
//        }
//    }
//}