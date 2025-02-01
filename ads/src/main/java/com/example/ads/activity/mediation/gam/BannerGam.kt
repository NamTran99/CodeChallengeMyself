package com.example.ads.activity.mediation.gam

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class BannerGam : IKSdkBaseBannerAd<AdManagerAdView>(AdNetwork.AD_MANAGER) {

    override fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {

            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.BANNER)
            if (adReady?.loadedAd != null) {
                showLogD("showAd cache1")

                showAdWithAdObject(
                    adReady,
                    screen,
                    scriptName,
                    showAdListener
                )
                return@launchWithSupervisorJob
            } else {
                showAdListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
                    scriptName,
                    adNetworkName
                )
            }
        }
    }

    override fun showAdWithAdObject(
        adReady: IKSdkBaseLoadedAd<AdManagerAdView>,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {

        adReady.loadedAd?.onPaidEventListener = adReady.loadedAd?.setupOnPaidEventListener(screen)
        showLogD("showAdWithAdObject start show")
        adReady.listener = object : IKAdActionCallback<AdManagerAdView, Any> {

            override fun onAdClicked(adNetwork: String) {
                showAdListener.onAdClick(scriptName, adNetworkName)
            }

            override fun onAdImpression(adNetwork: String) {
                showAdListener.onAdImpression(scriptName, adNetworkName)
            }
        }
        showLogD("showAdWithAdObject start show $screen")
        showAdListener.onAdReady(adReady, scriptName, adNetworkName)
    }


    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<AdManagerAdView>
    ) {
        showLogD("loadCoreAd pre start")
//        val context = IKSdkApplicationProvider.getContext()
//        if (context == null) {
//            callback.onAdFailedToLoad(
//                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
//            )
//            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
//            return
//        }

        val unitId = idAds.adUnitId?.trim()
        if (unitId.isNullOrBlank()) {
            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
            showLogD("loadCoreAd unit empty")
            return
        }
        if (!isLoadAndShow && checkLoadSameAd(
                idAds.adPriority ?: 0,
                idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.BANNER
            )
        ) {
            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
            showLogD("loadCoreAd an ad ready")
            return
        }
        showLogD("loadCoreAd start")
        var handleTimeout: IKSdkHandleTimeoutAd<AdManagerAdView>? =
            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
        var objectAd: IKSdkBaseLoadedAd<AdManagerAdView>? = null
//        val adView = AdManagerAdView(context)
//        adView.setAdSize(
//            getAdmobAdSize(context, screen)
//        )
//        adView.adUnitId = unitId
        val adRequest = AdManagerAdRequest.Builder().build()
        var loadCoreCallback: IKSdkLoadCoreAdCallback<AdManagerAdView>? =
            object : IKSdkLoadCoreAdCallback<AdManagerAdView> {
                override fun onLoaded(result: AdManagerAdView) {
                    showLogD("loadCoreAd onAdLoaded")
                    objectAd = createDto(showPriority, result, idAds)
                    handleTimeout?.onLoaded(this@BannerGam, coroutineScope, objectAd, scriptName)
                    handleTimeout = null
                }

                override fun onLoadFail(error: IKAdError) {
                    showLogD("loadCoreAd onAdFailedToLoad, $error")
                    handleTimeout?.onLoadFail(this@BannerGam, error, scriptName)
                    handleTimeout = null
                }
            }
//        adView.adListener = object : AdListener() {
//            override fun onAdClosed() {
//
//            }
//
//            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                loadCoreCallback?.onLoadFail(IKAdError(loadAdError))
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
//            override fun onAdImpression() {
//                showLogD("loadCoreAd onAdImpression")
//                objectAd?.listener?.onAdImpression(adNetworkName)
//            }
//        }

        withContext(Dispatchers.Main) {
//            adView.loadAd(adRequest)
        }
        handleTimeout?.startHandle(this, scriptName)
    }


    private fun AdManagerAdView?.setupOnPaidEventListener(screen: String) = OnPaidEventListener {
        val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.ADMOB_SDK)
        adRevenue.setRevenue(
            it.valueMicros / IKSdkDefConst.DATA_RV_D_DIV_USD, it.currencyCode
        )

        IKSdkTrackingHelper.customPaidAd(
            adNetwork = adNetworkName,
            revMicros = it.valueMicros.toDouble() / IKSdkDefConst.DATA_RV_D_DIV_USD,
            currency = it.currencyCode,
            adUnitId = this?.adUnitId ?: IKSdkDefConst.UNKNOWN,
            this?.responseInfo?.mediationAdapterClassName ?: IKSdkDefConst.UNKNOWN,
            adFormat = adFormatName,
            screen = screen
        )
        adRevenue.adRevenuePlacement = adFormatName
        adRevenue.adRevenueUnit = this?.adUnitId ?: IKSdkDefConst.UNKNOWN
        Adjust.trackAdRevenue(adRevenue)
    }

}