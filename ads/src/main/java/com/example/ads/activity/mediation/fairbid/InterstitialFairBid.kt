//package com.example.ads.activity.mediation.fairbid
//
//import android.app.Activity
//import com.adjust.sdk.Adjust
//import com.adjust.sdk.AdjustAdRevenue
//import com.fyber.fairbid.ads.ImpressionData
//import com.fyber.fairbid.ads.Interstitial
//import com.fyber.fairbid.ads.interstitial.InterstitialListener
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKCustomEventData
//import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
//import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
//import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
//import com.example.ads.activity.format.intertial.IKSdkBaseInterstitialAds
//import com.example.ads.activity.listener.sdk.IKAdActionCallback
//import com.example.ads.activity.listener.sdk.IKSdkAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkBaseListener
//import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.withContext
//
//class InterstitialFairBid : IKSdkBaseInterstitialAds<IKCustomEventData>(AdNetwork.AD_FAIR_BID) {
//    private var dataAds: ArrayList<IKCustomEventData> = ArrayList()
//    override fun showAd(
//        coroutineScope: CoroutineScope,
//        activity: Activity,
//        screen: String,
//        scriptName: String,
//        adsListener: IKSdkBaseListener
//    ) {
//        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
//            runCatching {
//                dataAds.sortByDescending { it.p ?: 0 }
//            }
//            dataAds.filter { Interstitial.isAvailable(it.getUnitValue()) }.forEach {
//                if (mListAd.find { dt -> it.getUnitValue() != dt.unitId } == null) {
//                    val objectAd = IKSdkBaseLoadedAd(
//                        it.getUnitValue(),
//                        loadedAd = it,
//                        adPriority = it.p ?: 0,
//                        showPriority = mListAd.firstAdOrNull(mutexListAd)?.showPriority ?: 0
//                    ).apply {
//                        this.adNetwork = adNetworkName
//                        this.adFormat = adFormatName
//                        this.lastTimeLoaded = System.currentTimeMillis()
//                    }
//                    addItemAds(
//                        objectAd
//                    )
//                }
//            }
//
//            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.INTER)
//            val unitId = adReady?.loadedAd?.getUnitValue() ?: ""
//            if (unitId.isBlank()) {
//                showLogD("not valid Ad")
//                adsListener.onAdShowFailed(
//                    adNetworkName,
//                    screen,
//                    scriptName,
//                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
//                )
//                return@launchWithSupervisorJob
//            }
//
//            if (!Interstitial.isAvailable(unitId)) {
//                showLogD("not valid Ad")
//                adsListener.onAdShowFailed(
//                    adNetworkName,
//                    screen,
//                    scriptName,
//                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
//                )
//                return@launchWithSupervisorJob
//            }
//            adReady?.listener = object : IKAdActionCallback<IKCustomEventData, Any> {
//
//                override fun onAdClicked(adNetwork: String) {
//                    showLogD("showAdOnAdClicked")
//                    adsListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady?.adPriority,
//                        adReady?.uuid ?: ""
//                    )
//                }
//
//                override fun onAdImpression(adNetwork: String) {
//
//                }
//
//                override fun onAdDismiss(adNetwork: String) {
//                    isAdShowing = false
//                    showLogD("onAdHidden")
//                    adsListener.onAdDismissed(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + (adReady?.adPriority ?: 0),
//                        adReady?.uuid ?: ""
//                    )
//                }
//
//                override fun onAdShowed(adNetwork: String) {
//                    isAdShowing = true
//                    showLogD("onAdDisplayed")
//                    adsListener.onAdShowed(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady?.adPriority,
//                        adReady?.adPriority ?: 0, adReady?.uuid ?: ""
//                    )
//                }
//
//                override fun onAdImpression(impressionData: Any?) {
//                    adsListener.onAdImpression(
//                        adNetworkName, screen, scriptName, adReady?.uuid ?: ""
//                    )
//                    (impressionData as? ImpressionData)?.setupOnPaidEventListener(unitId, screen)
//                }
//
//                override fun onAdShowFail(adNetwork: String, error: IKAdError) {
//                    isAdShowing = false
//                    adsListener.onAdShowFailed(
//                        adNetworkName,
//                        screen,
//                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + (adReady?.adPriority ?: 0),
//                        IKAdError(IKSdkErrorCode.SHOWING_FAIL)
//                    )
//                    showLogD("showAd onAdDisplayed error ")
//                }
//            }
//
//            showLogD("showAd start show")
//            Interstitial.show(unitId, activity)
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
//        showLogD("loadCoreAd pre start")
//        if (!IKFairBidHelper.isInitialized()) {
//            callback.onAdFailedToLoad(
//                adNetworkName,
//                IKAdError(IKSdkErrorCode.NETWORK_NOT_INITIALIZED)
//            )
//            showLogD("loadCoreAd ${IKSdkErrorCode.NETWORK_NOT_INITIALIZED.message}")
//            return
//        }
//        val unitId = idAds.adUnitId?.trim()
//
//        if (unitId.isNullOrBlank()) {
//            callback.onAdFailedToLoad(
//                adNetworkName,
//                IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID)
//            )
//            showLogD("loadCoreAd unit empty")
//            return
//        }
//        if (checkLoadSameAd(
//                idAds.adPriority ?: 0,
//                idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.INTER
//            )
//        ) {
//            callback.onAdFailedToLoad(
//                adNetworkName,
//                IKAdError(IKSdkErrorCode.READY_CURRENT_AD)
//            )
//            showLogD("loadCoreAd an ad ready")
//            return
//        }
//        showLogD("loadCoreAd start")
//        var handleTimeout: IKSdkHandleTimeoutAd<IKCustomEventData>? =
//            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
//        var objectAd: IKSdkBaseLoadedAd<IKCustomEventData>? = null
//
//        Interstitial.setInterstitialListener(object : InterstitialListener {
//            override fun onAvailable(placementId: String) {
//                showLogD("loadCoreAd onAdLoaded")
//                objectAd =
//                    createDto(
//                        showPriority,
//                        IKCustomEventData(
//                            idAds.adPriority ?: 0,
//                            placementId,
//                            IKSdkDefConst.TimeOutAd.LOAD_AD_TIME_OUT
//                        ),
//                        idAds
//                    )
//                handleTimeout?.onLoaded(
//                    this@InterstitialFairBid,
//                    coroutineScope,
//                    objectAd,
//                    scriptName
//                )
//                handleTimeout = null
//            }
//
//            override fun onClick(placementId: String) {
//                objectAd?.listener?.onAdClicked(adNetworkName)
//            }
//
//            override fun onHide(placementId: String) {
//                objectAd?.listener?.onAdDismiss(adNetworkName)
//            }
//
//            override fun onRequestStart(placementId: String, requestId: String) {
//
//            }
//
//            override fun onShow(placementId: String, impressionData: ImpressionData) {
//                objectAd?.listener?.onAdShowed(adNetworkName)
//                objectAd?.listener?.onAdImpression(impressionData)
//            }
//
//            override fun onShowFailure(placementId: String, impressionData: ImpressionData) {
//                objectAd?.listener?.onAdShowFail(
//                    adNetworkName,
//                    IKAdError(IKSdkErrorCode.SHOWING_FAIL)
//                )
//            }
//
//            override fun onUnavailable(placementId: String) {
//                handleTimeout?.onLoadFail(
//                    this@InterstitialFairBid,
//                    IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
//                    scriptName
//                )
//                handleTimeout = null
//            }
//        })
//        kotlin.runCatching {
//            if (dataAds.find { it.getUnitValue() == unitId } == null)
//                dataAds.add(IKCustomEventData(idAds.adPriority ?: 0, unitId, 0))
//        }
//        withContext(Dispatchers.Main) {
//            Interstitial.request(unitId)
//        }
//        handleTimeout?.startHandle(this, scriptName)
//    }
//
//    private fun ImpressionData.setupOnPaidEventListener(
//        adUnit: String, screen: String
//    ) {
//        val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.PUBLISHER_SDK)
//        adRevenue.setRevenue(
//            this.netPayout,
//            this.currency
//        )
//        adRevenue.adRevenueNetwork = this.demandSource
//        adRevenue.adRevenueUnit = adUnit
//        adRevenue.adRevenuePlacement = adFormatName
//        adRevenue.adRevenueUnit = adUnit
//        Adjust.trackAdRevenue(adRevenue)
//
//        IKSdkTrackingHelper.customPaidAd(
//            adNetwork = adNetworkName,
//            revMicros = this.netPayout,
//            currency = this.currency,
//            adUnitId = adUnit,
//            responseAdNetwork = placementType.name,
//            adFormat = adFormatName,
//            screen = screen
//        )
//    }
//
//    override fun loadAd(
//        coroutineScope: CoroutineScope,
//        adData: IKAdapterDto?,
//        callback: IKSdkLoadAdCoreListener?
//    ) {
//        if (IKFairBidHelper.initStatus)
//            super.loadAd(coroutineScope, adData, callback)
//        else {
//            coroutineScope.launchWithSupervisorJob {
//                delay(2000)
//                if (IKFairBidHelper.initStatus)
//                    super.loadAd(coroutineScope, adData, callback)
//                else delay(1200)
//                super.loadAd(coroutineScope, adData, callback)
//            }
//        }
//    }
//}