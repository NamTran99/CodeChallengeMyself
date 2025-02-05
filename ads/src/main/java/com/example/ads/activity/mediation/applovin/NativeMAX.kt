package com.example.ads.activity.mediation.applovin

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.native_ads.IKSdkBaseNativeAds
import com.example.ads.activity.format.native_ads.NativeBackUpLatest
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay


class NativeMAX : IKSdkBaseNativeAds<IkObjectNativeMax>(
    AdNetwork.AD_MAX
) {

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<IkObjectNativeMax>
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
                idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.NATIVE
            )
        ) {
            callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
            showLogD("loadCoreAd an ad ready")
            return
        }
        showLogD("loadCoreAd start")
        var handleTimeout: IKSdkHandleTimeoutAd<IkObjectNativeMax>? =
            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
        var objectAd: IKSdkBaseLoadedAd<IkObjectNativeMax>? = null
        var loadCoreCallback: IKSdkLoadCoreAdCallback<IkObjectNativeMax>? =
            object : IKSdkLoadCoreAdCallback<IkObjectNativeMax> {
                override fun onLoaded(result: IkObjectNativeMax) {
                    objectAd = createDto(showPriority, result, idAds)
                    handleTimeout?.onLoaded(this@NativeMAX, coroutineScope, objectAd, scriptName)
                    handleTimeout = null

                    // store objectAd
                    NativeBackUpLatest.addBackupAdLatest(objectAd)
                }

                override fun onLoadFail(error: IKAdError) {
                    handleTimeout?.onLoadFail(this@NativeMAX, error, scriptName)
                    handleTimeout = null
                }
            }

//        val adLoader = MaxNativeAdLoader(unitId, context)


//        adLoader.setNativeAdListener(object : MaxNativeAdListener() {
//            override fun onNativeAdLoaded(p0: MaxNativeAdView?, p1: MaxAd) {
//                loadCoreCallback?.onLoaded(IkObjectNativeMax(adLoader, p1))
//                loadCoreCallback = null
//                showLogD("loadCoreAd onAdLoaded")
//
//            }
//
//            override fun onNativeAdLoadFailed(p0: String, p1: MaxError) {
//                showLogD("loadCoreAd onAdFailedToLoad, $p1")
//                loadCoreCallback?.onLoadFail(IKAdError(p1))
//                loadCoreCallback = null
//            }
//
//            override fun onNativeAdClicked(p0: MaxAd) {
//                super.onNativeAdClicked(p0)
//                showLogD("loadCoreAd onAdClicked")
//                objectAd?.listener?.onAdClicked(adNetworkName)
//            }
//
//        })
//        adLoader.loadAd()
        handleTimeout?.startHandle(this, scriptName)
    }

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
                    scriptName,
                    screen,
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
        adReady: IKSdkBaseLoadedAd<IkObjectNativeMax>,
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {

        adReady.loadedAd?.loader?.setRevenueListener(
            setupOnPaidEventListener(
                screen,
                scriptName,
                showAdListener
            )
        )
        showLogD("showAdWithAdObject start show")
        adReady.listener = object : IKAdActionCallback<IkObjectNativeMax, Any> {
            override fun onAdClicked(adNetwork: String) {
                showAdListener.onAdClick(
                    scriptName,
                    adNetworkName
                )
            }

            override fun onAdImpression(adNetwork: String) {
            }
        }
        showLogD("showAdWithAdObject start show $screen")
        showAdListener.onAdReady(adReady, scriptName, adNetworkName)
    }

    override fun showAdWithAdView(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        adObject: IkmDisplayWidgetAdView,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        if (adObject.adObject == null) {
            showAdListener.onAdShowFail(
                IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
                scriptName,
                adNetworkName
            )
            return
        }
        coroutineScope.launchWithSupervisorJob {
            showLogD("showAd pre show")
            @Suppress("UNCHECKED_CAST")
            val adReady = adObject.adObject as? IKSdkBaseLoadedAd<IkObjectNativeMax>
            if (adReady?.loadedAd == null) {
                showLogD("showAd pre not valid Ad,loadS 1")
                showAdListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
                    scriptName,
                    adNetworkName
                )
                return@launchWithSupervisorJob
            }
            showAdWithAdObject(
                adReady,
                scriptName,
                screen,
                showAdListener
            )

            return@launchWithSupervisorJob
        }
    }

    private fun setupOnPaidEventListener(
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) = MaxAdRevenueListener {
        val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.APPLOVIN_MAX_SDK)
        adRevenue.setRevenue(it.revenue, IKSdkDefConst.CURRENCY_CODE_USD)
        adRevenue.adRevenueNetwork = it.networkName
        adRevenue.adRevenueUnit = it.adUnitId
        adRevenue.adRevenuePlacement = adFormatName
        adRevenue.adRevenueUnit = it.adUnitId ?: IKSdkDefConst.UNKNOWN
        Adjust.trackAdRevenue(adRevenue)

        IKSdkTrackingHelper.customPaidAd(
            adNetwork = adNetworkName,
            revMicros = it.revenue,
            currency = IKSdkDefConst.CURRENCY_CODE_USD,
            adUnitId = it.adUnitId,
            responseAdNetwork = it.networkName ?: IKSdkDefConst.UNKNOWN,
            adFormat = adFormatName,
            screen = screen
        )
        runCatching {
            showAdListener.onAdImpression(
                scriptName, adNetworkName
            )
        }
    }

    override fun loadAd(
        coroutineScope: CoroutineScope,
        adData: IKAdapterDto?,
        callback: IKSdkLoadAdCoreListener?
    ) {
        if (IKApplovinHelper.initStatus)
            super.loadAd(coroutineScope, adData, callback)
        else {
            coroutineScope.launchWithSupervisorJob {
                var elapsedTime = 0L
                while (elapsedTime < IKSdkDefConst.MediationTime.TOTAL_TIME) {
                    if (IKApplovinHelper.initStatus) {
                        super.loadAd(coroutineScope, adData, callback)
                        return@launchWithSupervisorJob
                    }
                    delay(IKSdkDefConst.MediationTime.SPLIT_TIME)
                    elapsedTime += IKSdkDefConst.MediationTime.SPLIT_TIME
                }
                super.loadAd(coroutineScope, adData, callback)
            }
        }
    }
}