//package com.example.ads.activity.mediation.fairbid
//
//import com.adjust.sdk.Adjust
//import com.adjust.sdk.AdjustAdRevenue
//import com.fyber.fairbid.ads.ImpressionData
//import com.fyber.fairbid.ads.banner.BannerError
//import com.fyber.fairbid.ads.banner.BannerListener
//import com.fyber.fairbid.ads.banner.BannerOptions
//import com.fyber.fairbid.ads.banner.BannerView
//import com.example.ads.activity.core.IKSdkApplicationProvider
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
//import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
//import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
//import com.example.ads.activity.listener.sdk.IKAdActionCallback
//import com.example.ads.activity.listener.sdk.IKSdkAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//open class BannerFairBid : IKSdkBaseBannerAd<BannerView>(AdNetwork.AD_FAIR_BID) {
//    override fun showAvailableAd(
//        coroutineScope: CoroutineScope,
//        screen: String,
//        scriptName: String,
//        showAdListener: IKSdkShowWidgetAdListener
//    ) {
//        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
//
//            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.BANNER)
//            if (adReady?.loadedAd != null) {
//
//                showLogD("showAd cache1")
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
//        }
//    }
//
//    override fun showAdWithAdObject(
//        adReady: IKSdkBaseLoadedAd<BannerView>,
//        screen: String,
//        scriptName: String,
//        showAdListener: IKSdkShowWidgetAdListener
//    ) {
//        showLogD("showAdWithAdObject start show")
//        adReady.listener = object : IKAdActionCallback<BannerView, Any> {
//
//            override fun onAdClicked(adNetwork: String) {
//                showAdListener.onAdClick(
//                    scriptName,
//                    adNetworkName
//                )
//            }
//
//            override fun onAdImpression(adNetwork: String) {
//            }
//
//            override fun onAdImpression(adUnit: String, impressionData: Any?) {
//                showAdListener.onAdImpression(scriptName, adNetworkName)
//                (impressionData as? ImpressionData)?.setupOnPaidEventListener(adUnit, screen)
//            }
//        }
//        showLogD("showAdWithAdObject start show $screen")
//        showAdListener.onAdReady(adReady, scriptName, adNetworkName)
//    }
//
//    override suspend fun loadCoreAd(
//        coroutineScope: CoroutineScope,
//        idAds: IKAdUnitDto,
//        scriptName: String,
//        screen: String?,
//        showPriority: Int,
//        isLoadAndShow: Boolean,
//        callback: IKSdkAdCallback<BannerView>
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
//        if (!IKFairBidHelper.isInitialized()) {
//            callback.onAdFailedToLoad(
//                adNetworkName,
//                IKAdError(IKSdkErrorCode.NETWORK_NOT_INITIALIZED)
//            )
//            showLogD("loadCoreAd ${IKSdkErrorCode.NETWORK_NOT_INITIALIZED.message}")
//            return
//        }
//        val unitId = idAds.adUnitId?.trim()
//        if (unitId.isNullOrBlank()) {
//            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//            showLogD("loadCoreAd unit empty")
//            return
//        }
//        if (!isLoadAndShow && checkLoadSameAd(
//                idAds.adPriority ?: 0,
//                idAds.cacheSize ?: 0,
//                IKSdkDefConst.TimeOutAd.BANNER
//            )
//        ) {
//            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
//            showLogD("loadCoreAd an ad ready")
//            return
//        }
//        showLogD("loadCoreAd start")
//        var handleTimeout: IKSdkHandleTimeoutAd<BannerView>? =
//            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
//        var objectAd: IKSdkBaseLoadedAd<BannerView>? = null
//
//        val adView = BannerView(context, unitId)
//
//        adView.bannerListener = object : BannerListener {
//            override fun onClick(placementId: String) {
//                objectAd?.listener?.onAdClicked(adNetworkName)
//            }
//
//            override fun onError(placementId: String, error: BannerError) {
//                handleTimeout?.onLoadFail(
//                    this@BannerFairBid,
//                    IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
//                    scriptName
//                )
//                handleTimeout = null
//            }
//
//            override fun onLoad(placementId: String) {
//                showLogD("loadCoreAd onAdLoaded")
//                objectAd =
//                    createDto(
//                        showPriority,
//                        adView,
//                        idAds
//                    )
//                handleTimeout?.onLoaded(
//                    this@BannerFairBid,
//                    coroutineScope,
//                    objectAd,
//                    scriptName
//                )
//                handleTimeout = null
//            }
//
//            override fun onRequestStart(placementId: String, requestId: String) {
//
//            }
//
//            override fun onShow(placementId: String, impressionData: ImpressionData) {
//                objectAd?.listener?.onAdShowed(adNetworkName)
//                objectAd?.listener?.onAdImpression(placementId, impressionData)
//            }
//
//        }
//        withContext(Dispatchers.Main) {
//            val bannerOptions = BannerOptions().setAdaptive(true)
//            adView.load(bannerOptions)
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
//}