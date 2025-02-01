package com.example.ads.activity.mediation.admob

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.native_ads.IKSdkBaseNativeAds
import com.example.ads.activity.format.native_ads.NativeBackUpLatest
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


open class NativeAdmob : IKSdkBaseNativeAds<NativeAd>(AdNetwork.AD_MOB) {

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<NativeAd>
    ) {
        showLogD("loadCoreAd pre start")
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            callback.onAdFailedToLoad(
                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
            )
            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
            return
        }
        coroutineScope.launchWithSupervisorJob(Dispatchers.IO) {
            val unitId = idAds.adUnitId?.trim()
            if (unitId.isNullOrBlank()) {
                callback.onAdFailedToLoad(
                    adNetworkName,
                    IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD)
                )
                showLogD("loadCoreAd unit empty")
                return@launchWithSupervisorJob
            }
            if (!isLoadAndShow && checkLoadSameAd(
                    idAds.adPriority ?: 0,
                    idAds.cacheSize ?: 0,
                    IKSdkDefConst.TimeOutAd.NATIVE
                )
            ) {
                IKLogs.d("cnnnnad_show") { "size=${mListAd.size}" }
                callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
                showLogD("loadCoreAd an ad ready")
                return@launchWithSupervisorJob
            }
            showLogD("loadCoreAd start")

            var handleTimeout: IKSdkHandleTimeoutAd<NativeAd>? =
                IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)

            var objectAd: IKSdkBaseLoadedAd<NativeAd>? = null

            var loadCoreCallback: IKSdkLoadCoreAdCallback<NativeAd>? =
                object : IKSdkLoadCoreAdCallback<NativeAd> {
                    override fun onLoaded(result: NativeAd) {
                        objectAd = createDto(showPriority, result, idAds)
                        handleTimeout?.onLoaded(
                            this@NativeAdmob,
                            coroutineScope,
                            objectAd,
                            scriptName
                        )
                        handleTimeout = null

                        // store objectAd
                        NativeBackUpLatest.addBackupAdLatest(objectAd)
                    }

                    override fun onLoadFail(error: IKAdError) {
                        handleTimeout?.onLoadFail(this@NativeAdmob, error, scriptName)
                        handleTimeout = null
                    }
                }
//            val builder = AdLoader.Builder(context, unitId)
//            builder.forNativeAd { unifiedNativeAd ->
//                loadCoreCallback?.onLoaded(unifiedNativeAd)
//                loadCoreCallback = null
//            }
            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
//            builder.withNativeAdOptions(adOptions)

//            val adLoader = builder.withAdListener(object : AdListener() {
//                override fun onAdClosed() {
//
//                }
//
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    showLogD("loadCoreAd onAdFailedToLoad, $loadAdError")
//                    loadCoreCallback?.onLoadFail(IKAdError(loadAdError))
//                    loadCoreCallback = null
//                }
//
//                override fun onAdOpened() {
//
//                }
//
//                override fun onAdLoaded() {
//                    showLogD("loadCoreAd onAdLoaded")
//                }
//
//                override fun onAdClicked() {
//                    showLogD("loadCoreAd onAdClicked")
//                    objectAd?.listener?.onAdClicked(adNetworkName)
//                }
//
//                override fun onAdImpression() {
//                    showLogD("loadCoreAd onAdImpression")
//                    objectAd?.listener?.onAdImpression(adNetworkName)
//                }
//            }).build()

//            adLoader.loadAd(AdRequest.Builder().build())
            handleTimeout?.startHandle(this@NativeAdmob, scriptName)
        }
    }

    override fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {

            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.NATIVE)
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
        adReady: IKSdkBaseLoadedAd<NativeAd>,
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        adReady.loadedAd?.setOnPaidEventListener(
            adReady.loadedAd?.setupOnPaidEventListener(
                adReady.unitId ?: IKSdkDefConst.UNKNOWN,
                screen
            )
        )
        showLogD("showAdWithAdObject start show")
        adReady.listener = object : IKAdActionCallback<NativeAd, Any> {
            override fun onAdClicked(adNetwork: String) {
                showAdListener.onAdClick(
                    scriptName, adNetworkName
                )
            }

            override fun onAdImpression(adNetwork: String) {
                showAdListener.onAdImpression(
                   scriptName, adNetworkName
                )
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
            val adReady = adObject.adObject as? IKSdkBaseLoadedAd<NativeAd>?
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


    protected fun NativeAd?.setupOnPaidEventListener(adUnitId: String, screen: String) =
        OnPaidEventListener {
            val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.ADMOB_SDK)
            adRevenue.setRevenue(
                it.valueMicros / IKSdkDefConst.DATA_RV_D_DIV_USD, it.currencyCode
            )
            IKSdkTrackingHelper.customPaidAd(
                adNetwork = adNetworkName,
                revMicros = it.valueMicros.toDouble() / IKSdkDefConst.DATA_RV_D_DIV_USD,
                currency = it.currencyCode,
                adUnitId = adUnitId,
                responseAdNetwork = this?.responseInfo?.mediationAdapterClassName
                    ?: IKSdkDefConst.UNKNOWN,
                adFormat = adFormatName,
                screen = screen
            )
            adRevenue.adRevenuePlacement = adFormatName
            adRevenue.adRevenueUnit = adUnitId
            Adjust.trackAdRevenue(adRevenue)
        }
}