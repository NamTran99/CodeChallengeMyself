//package com.example.ads.activity.mediation.playgap
//
//import android.app.Activity
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
//import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
//import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
//import com.example.ads.activity.listener.sdk.IKAdActionCallback
//import com.example.ads.activity.listener.sdk.IKSdkAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
//import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IkmSdkCoreFunc
////import io.playgap.sdk.AdViewListener
////import io.playgap.sdk.PlaygapAdView
////import io.playgap.sdk.PlaygapAds
////import io.playgap.sdk.ShowError
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import java.lang.ref.WeakReference
//
//open class BannerPlayGap : IKSdkBaseBannerAd<PlaygapAdView>(AdNetwork.PLAYGAP) {
//
//    override fun showAvailableAd(
//        coroutineScope: CoroutineScope,
//        screen: String,
//        scriptName: String,
//        showAdListener: IKSdkShowWidgetAdListener
//    ) {
//        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
//            if (!IKPlayGapHelper.initStatus) {
//                showAdListener.onAdShowFail(
//                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
//                    scriptName,
//                    adNetworkName
//                )
//                return@launchWithSupervisorJob
//            }
//            val activity = IkmSdkCoreFunc.AppF.listActivity.values.firstOrNull()
//            if (activity == null) {
//                showAdListener.onAdShowFail(
//                    IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID),
//                    scriptName,
//                    adNetworkName
//                )
//            }
//            val adReady: IKSdkBaseLoadedAd<PlaygapAdView> = IKSdkBaseLoadedAd()
//            val currentActivity =
//                WeakReference<Activity>(activity).get()
//            if (currentActivity == null) {
//                showAdListener.onAdShowFail(
//                    IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID),
//                    scriptName,
//                    adNetworkName
//                )
//                return@launchWithSupervisorJob
//            }
//            val playgapAds = PlaygapAds.createBanner(currentActivity, object :
//                AdViewListener {
//                override fun onShowFailed(error: ShowError) {
//                    showAdListener.onAdShowFail(IKAdError(error), scriptName, adNetworkName)
//                }
//
//                override fun onShowImpression(impressionId: String) {
//                    // Banner show impression
//                }
//            })
//            adReady.loadedAd = playgapAds
//
//            if (adReady.loadedAd != null) {
//                showLogD("showAd cache1")
//                adReady.loadedAd?.show(currentActivity)
//                adReady.loadedAd?.enableAutomaticNotchSupport(currentActivity)
//                showAdWithAdObject(
//                    adReady,
//                    screen,
//                    scriptName,
//                    showAdListener
//                )
//                return@launchWithSupervisorJob
//            } else {
//                showAdListener.onAdShowFail(
//                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
//                    scriptName,
//                    adNetworkName
//                )
//            }
//
//
//        }
//    }
//
//    override fun showAdWithAdObject(
//        adReady: IKSdkBaseLoadedAd<PlaygapAdView>,
//        screen: String,
//        scriptName: String,
//        showAdListener: IKSdkShowWidgetAdListener
//    ) {
//        showLogD("showAdWithAdObject start show")
//        adReady.listener = object : IKAdActionCallback<PlaygapAdView, Any> {
//
//            override fun onAdClicked(adNetwork: String) {
//                showAdListener.onAdClick(scriptName, adNetworkName)
//            }
//
//            override fun onAdImpression(adNetwork: String) {
//                showAdListener.onAdImpression(scriptName, adNetworkName)
//            }
//        }
//        showLogD("showAdWithAdObject start show $screen")
//        showAdListener.onAdReady(adReady, scriptName, adNetworkName)
//    }
//
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
//
//    override suspend fun loadCoreAd(
//        coroutineScope: CoroutineScope,
//        idAds: IKAdUnitDto,
//        scriptName: String,
//        screen: String?,
//        showPriority: Int,
//        isLoadAndShow: Boolean,
//        callback: IKSdkAdCallback<PlaygapAdView>
//    ) {
//
//    }
//
//}