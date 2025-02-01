//package com.example.ads.activity.mediation.ikad
//
//import android.view.Gravity
//import android.widget.FrameLayout
//import com.adjust.sdk.Adjust
//import com.adjust.sdk.AdjustAdRevenue
////import com.example.ads.activity.ads.listener.pub.IKameAdListener
//import com.example.ads.activity.ads.model.IKameAdError
////import com.example.ads.activity.ads.model.banner.IKameAdView
//import com.example.ads.activity.core.IKSdkApplicationProvider
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
//import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
//import com.example.ads.activity.listener.sdk.IKAdActionCallback
//import com.example.ads.activity.listener.sdk.IKSdkAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
//import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.util.concurrent.atomic.AtomicBoolean
//
//open class BannerIkAd : IKSdkBaseBannerAd<IKameAdView>(AdNetwork.AD_IK) {
//
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
//                showLogD("showAd cache1")
//
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
//        adReady: IKSdkBaseLoadedAd<IKameAdView>,
//        screen: String,
//        scriptName: String,
//        showAdListener: IKSdkShowWidgetAdListener
//    ) {
//        showLogD("showAdWithAdObject start show")
//        adReady.listener = object : IKAdActionCallback<IKameAdView, Any> {
//
//            override fun onAdClicked(adNetwork: String) {
//                showAdListener.onAdClick(scriptName, adNetworkName)
//            }
//
//            override fun onAdImpression(adNetwork: String) {
//                showAdListener.onAdImpression(scriptName, adNetworkName)
//                adReady.loadedAd?.setupOnPaidEventListener(
//                    adReady.unitId ?: IKSdkDefConst.UNKNOWN,
//                    screen
//                )
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
//        callback: IKSdkAdCallback<IKameAdView>
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
//            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//            showLogD("loadCoreAd unit empty")
//            return
//        }
//        if (isAdReady(idAds.adPriority ?: 0, IKSdkDefConst.TimeOutAd.NATIVE)) {
//            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
//            showLogD("loadCoreAd an ad ready")
//            return
//        }
//        showLogD("loadCoreAd start")
//        val startLoadTime = System.currentTimeMillis()
//
//        val adView = IKameAdView(context)
//
//        adView.adUnitId = unitId
//
//        val isEventHandled = AtomicBoolean(false)
//        var objectAd: IKSdkBaseLoadedAd<IKameAdView>? = null
//        var loadCoreCallback: IKSdkLoadCoreAdCallback<IKameAdView>? =
//            object : IKSdkLoadCoreAdCallback<IKameAdView> {
//                override fun onLoaded(result: IKameAdView) {
//                    showLogD("loadCoreAd onAdLoaded")
//                    if (!isEventHandled.compareAndSet(false, true))
//                        return
//                    objectAd = createDto(showPriority, adView, idAds)
//                    callback.onAdLoaded(adNetworkName, objectAd)
//                    trackAdLoaded(startLoadTime, idAds.adPriority ?: 0, unitId, scriptName)
//                }
//
//                override fun onLoadFail(error: IKAdError) {
//                    showLogD("loadCoreAd onAdFailedToLoad")
//                    if (!isEventHandled.compareAndSet(false, true))
//                        return
//                    callback.onAdFailedToLoad(adNetworkName, error)
//                    trackAdLoadFail(
//                        startLoadTime, idAds.adPriority ?: 0, unitId,
//                        scriptName, error.message,
//                        "${error.code}"
//                    )
//                }
//            }
//
//        adView.adListener = object : IKameAdListener {
//            override fun onAdClosed() {
//
//            }
//
//            override fun onAdFailedToLoad(error: IKameAdError) {
//                loadCoreCallback?.onLoadFail(IKAdError(error))
//                loadCoreCallback = null
//            }
//
//            override fun onAdOpened() {
//
//            }
//
//            override fun onAdLoaded() {
//                loadCoreCallback?.onLoaded(adView)
//                loadCoreCallback = null
//            }
//
//            override fun onAdClicked() {
//                showLogD("loadCoreAd onAdClicked")
//                objectAd?.listener?.onAdClicked(adNetworkName)
//            }
//
//            override fun onAdImpression(adId: String) {
//                showLogD("loadCoreAd onAdImpression")
//                objectAd?.listener?.onAdImpression(adNetworkName)
//            }
//        }
//
//        withContext(Dispatchers.Main) {
//            adView.loadAd()
//        }
//    }
//
//
//    private fun IKameAdView?.setupOnPaidEventListener(adUnitId: String, screen: String) {
//        val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.PUBLISHER_SDK)
//        adRevenue.setRevenue(
//            this?.revenue ?: 0.0,
//            IKSdkDefConst.CURRENCY_CODE_USD
//        )
//        adRevenue.adRevenueNetwork = this?.networkName ?: IKSdkDefConst.UNKNOWN
//        adRevenue.adRevenueUnit = adUnitId
//        adRevenue.adRevenuePlacement = adFormatName
//        adRevenue.adRevenueUnit = this?.adUnitId ?: IKSdkDefConst.UNKNOWN
//        Adjust.trackAdRevenue(adRevenue)
//
//        IKSdkTrackingHelper.customPaidAd(
//            adNetwork = adNetworkName,
//            revMicros = this?.revenue ?: 0.0,
//            currency = IKSdkDefConst.CURRENCY_CODE_USD,
//            adUnitId = adUnitId,
//            this?.networkName ?: IKSdkDefConst.UNKNOWN,
//            adFormat = adFormatName,
//            adId = this?.adId ?: "",
//            screen = screen
//        )
//    }
//}