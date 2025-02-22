//package com.example.ads.activity.mediation.ikad
//
//import android.app.Activity
//import com.adjust.sdk.Adjust
//import com.adjust.sdk.AdjustAdRevenue
//import com.example.ads.activity.ads.listener.pub.IKameAdFullScreenCallback
//import com.example.ads.activity.ads.listener.pub.IKameAdOpenLoadCallback
//import com.example.ads.activity.ads.model.IKameAdError
//import com.example.ads.activity.ads.model.open.IKameOpenAd
//import com.example.ads.activity.core.IKSdkApplicationProvider
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
//import com.example.ads.activity.format.open_ads.IKSdkBaseAppOpenAd
//import com.example.ads.activity.listener.sdk.IKSdkAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkBaseListener
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class OpenIkAd : IKSdkBaseAppOpenAd<IKameOpenAd>(AdNetwork.AD_IK) {
//
//    override suspend fun showAd(
//        coroutineScope: CoroutineScope,
//        activity: Activity,
//        screen: String,
//        adsListener: IKSdkBaseListener
//    ) {
//        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
////            if (isAdShowing) {
////                showLogD("showAd is Ad showing")
////                adsListener.onAdShowFailed(
////                    adNetworkName,
////                    screen,
////                    IKSdkDefConst.TXT_SCRIPT_SHOW,
////                    IKAdError(IKSdkErrorCode.OTHER_ADS_SHOWING)
////                )
////                return@launch
////            }
//            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.OPEN)
//            if (adReady?.loadedAd == null) {
//                adsListener.onAdShowFailed(
//                    adNetworkName,
//                    screen,
//                    IKSdkDefConst.TXT_SCRIPT_SHOW,
//                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
//                )
//                showLogD("not valid Ad")
//                return@launchWithSupervisorJob
//            }
//            showLogD("showAd start show")
//            adReady.loadedAd?.fullScreenAdCallback = object : IKameAdFullScreenCallback {
//
//                override fun onAdDismissed() {
//                    isAdShowing = false
//                    showLogD("showAdOnAdDismissedFullScreenContent")
//                    adsListener.onAdDismissed(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
//                        adReady.uuid
//                    )
//                }
//
//                override fun onAdFailedToShow(error: IKameAdError) {
//                    isAdShowing = false
//                    adsListener.onAdShowFailed(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
//                        IKAdError(error)
//                    )
//                    showLogD("showAd onAdFailedToShowFullScreenContent error ")
//                }
//
//                override fun onAdShowed() {
//                    isAdShowing = true
//                    showLogD("showAdOnAdShowedFullScreenContent")
//                    adsListener.onAdShowed(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
//                        adReady.adPriority, adReady.uuid
//                    )
//                }
//
//                override fun onAdImpression(adId: String) {
//                    showLogD("showAdOnAdImpression")
//                    adsListener.onAdImpression(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
//                        adReady.uuid
//                    )
//                    setupOnPaidEventListener(adReady.loadedAd, adId, screen)
//                }
//
//                override fun onAdClicked() {
//                    showLogD("showAdOnAdClicked")
//                    adsListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
//                        adReady.uuid
//                    )
//                }
//            }
//            adReady.loadedAd?.show(activity)
//        }
//
//    }
//
//    override suspend fun loadCoreAd(
//        coroutineScope: CoroutineScope,
//        idAds: IKAdUnitDto,
//        scriptName: String,
//        screen: String?,
//        showPriority: Int,
//        isLoadAndShow: Boolean,
//        callback: IKSdkAdCallback<IKameOpenAd>
//    ) {
//        showLogD("loadCoreAd pre start")
//        val context = IKSdkApplicationProvider.getContext()
//        if (context == null) {
//            callback.onAdFailedToLoad(
//                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
//            )
//            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
//            return
//        }
//        val unitId = idAds.adUnitId?.trim()
//        if (unitId.isNullOrBlank()) {
//            callback.onAdFailedToLoad(
//                adNetworkName,
//                IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID)
//            )
//            showLogD("loadCoreAd unit empty")
//            return
//        }
//        if (isAdReady(idAds.adPriority ?: 0, IKSdkDefConst.TimeOutAd.OPEN)) {
//            callback.onAdFailedToLoad(
//                adNetworkName,
//                IKAdError(IKSdkErrorCode.READY_CURRENT_AD)
//            )
//            showLogD("loadCoreAd an ad ready")
//            return
//        }
//        showLogD("loadCoreAd start")
//        val startLoadTime = System.currentTimeMillis()
//        var objectAd: IKSdkBaseLoadedAd<IKameOpenAd>? = null
//        val callbackLoad = object : IKameAdOpenLoadCallback {
//
//            override fun onAdFailedToLoad(error: IKameAdError) {
//                showLogD("loadCoreAd onAdFailedToLoad, $error")
//                val err = IKAdError(error)
//                callback.onAdFailedToLoad(adNetworkName, err)
//                trackAdLoadFail(
//                    startLoadTime, idAds.adPriority ?: 0, unitId,
//                    scriptName, error.message,
//                    "${error.code}"
//                )
//            }
//
//            override fun onAdLoaded(ads: IKameOpenAd) {
//                showLogD("loadCoreAd onAdLoaded")
//                objectAd = createDto(showPriority, ads, idAds)
//                callback.onAdLoaded(adNetworkName, objectAd)
//                trackAdLoaded(startLoadTime, idAds.adPriority ?: 0, unitId, scriptName)
//            }
//        }
//        withContext(Dispatchers.Main) {
//            IKameOpenAd().load(
//                context,
//                unitId,
//                callbackLoad
//            )
//        }
//    }
//
//    private fun setupOnPaidEventListener(
//        adsObject: IKameOpenAd?,
//        adId: String,
//        screen: String
//    ) {
//        val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.PUBLISHER_SDK)
//        adRevenue.setRevenue(
//            adsObject?.revenue ?: 0.0,
//            IKSdkDefConst.CURRENCY_CODE_USD
//        )
//        adRevenue.adRevenueNetwork = adsObject?.networkName ?: IKSdkDefConst.UNKNOWN
//        adRevenue.adRevenueUnit = adsObject?.adUnitId
//        adRevenue.adRevenuePlacement = adFormatName
//        adRevenue.adRevenueUnit = adsObject?.adUnitId ?: IKSdkDefConst.UNKNOWN
//        Adjust.trackAdRevenue(adRevenue)
//
//        IKSdkTrackingHelper.customPaidAd(
//            adNetwork = adNetworkName,
//            revMicros = adsObject?.revenue ?: 0.0,
//            currency = IKSdkDefConst.CURRENCY_CODE_USD,
//            adUnitId = adsObject?.adUnitId ?: IKSdkDefConst.UNKNOWN,
//            responseAdNetwork = adsObject?.networkName
//                ?: IKSdkDefConst.UNKNOWN,
//            adFormat = adFormatName,
//            adId,
//            screen = screen
//        )
//    }
//}