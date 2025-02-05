package com.example.ads.activity.mediation.applovin

import android.app.Activity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.intertial.IKSdkBaseInterstitialAds
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class InterstitialMax : IKSdkBaseInterstitialAds<MaxInterstitialAd>(AdNetwork.AD_MAX) {
    private var mCurrentCallbackObj: IKAdActionCallback<*, *>? = null
    override fun showAd(
        coroutineScope: CoroutineScope,
        activity: Activity,
        screen: String,
        scriptName: String,
        adsListener: IKSdkBaseListener
    ) {
//        if (this.isAdShowing) {
//            this.showLogD("showAd is Ad showing")
//            adsListener.onAdShowFailed(
//                this.adNetworkName,
//                screen,
//                scriptName,
//                IKAdError(IKSdkErrorCode.OTHER_ADS_SHOWING)
//            )
//            return
//        }
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.INTER)
            if (adReady?.loadedAd == null) {
                showLogD("not valid Ad")
                adsListener.onAdShowFailed(
                    adNetworkName,
                    screen,
                    scriptName,
                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
                )
                return@launchWithSupervisorJob
            }
            adReady.loadedAd?.setRevenueListener(setupOnPaidEventListener(screen))
            var adDismiss: (() -> Unit)? = {
                isAdShowing = false
                showLogD("onAdHidden")
                adsListener.onAdDismissed(
                    adNetworkName,
                    screen,
                    IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                )
                adReady.loadedAd?.destroy()
                adReady.loadedAd?.setListener(null)
                adReady.loadedAd?.setRevenueListener(null)
                mCurrentCallbackObj = null
                adReady.loadedAd = null
            }
            var onShowFail: ((p1: IKAdError?) -> Unit)? = { p1 ->
                isAdShowing = false
                adsListener.onAdShowFailed(
                    adNetworkName,
                    screen,
                    IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority,
                    p1 ?: IKAdError(IKSdkErrorCode.SHOWING_FAIL)
                )
                adReady.loadedAd?.destroy()
                adReady.loadedAd?.setListener(null)
                adReady.loadedAd?.setRevenueListener(null)
                adReady.loadedAd = null
                showLogD("showAd onAdDisplayed error:$p1 ")
            }
            adReady.listener = object : IKAdActionCallback<MaxInterstitialAd, Any> {
                override fun onAdClicked(adNetwork: String) {
                }

                override fun onAdImpression(adNetwork: String) {
                }

                override fun onAdDismiss(adNetwork: String) {
                    adDismiss?.invoke()
                    adDismiss = null
                }

                override fun onAdShowFail(adNetwork: String, error: IKAdError) {
                    onShowFail?.invoke(error)
                    onShowFail = null
                }
            }
            showLogD("showAd start show")
            adReady.loadedAd?.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {

                }

                override fun onAdDisplayed(p0: MaxAd) {
                    isAdShowing = true
                    showLogD("onAdDisplayed")
                    adsListener.onAdShowed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority,
                        adReady.adPriority, adReady.uuid
                    )

                    showLogD("showAdOnAdImpression")
                    adsListener.onAdImpression(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdHidden(p0: MaxAd) {
                    adDismiss?.invoke()
                    adDismiss = null
                }

                override fun onAdClicked(p0: MaxAd) {
                    showLogD("showAdOnAdClicked")
                    adsListener.onAdClicked(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {

                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    onShowFail?.invoke(null)
                    onShowFail = null
                }

            })
            mCurrentCallbackObj = adReady.listener
            adReady.loadedAd?.showAd(activity)
        }
    }

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<MaxInterstitialAd>
    ) {
        showLogD("loadCoreAd pre start")
        val unitId = idAds.adUnitId?.trim()
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            callback.onAdFailedToLoad(
                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
            )
            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
            return
        }
        if (unitId.isNullOrBlank()) {
            callback.onAdFailedToLoad(
                adNetworkName,
                IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID)
            )
            showLogD("loadCoreAd unit empty")
            return
        }
        if (checkLoadSameAd(
                idAds.adPriority ?: 0,
                idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.INTER
            )
        ) {
            callback.onAdFailedToLoad(
                adNetworkName,
                IKAdError(IKSdkErrorCode.READY_CURRENT_AD)
            )
            showLogD("loadCoreAd an ad ready")
            return
        }
        showLogD("loadCoreAd start")
        var handleTimeout: IKSdkHandleTimeoutAd<MaxInterstitialAd>? =
            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
        var objectAd: IKSdkBaseLoadedAd<MaxInterstitialAd>? = null
//        val interstitialAd = MaxInterstitialAd(unitId, context)
//        interstitialAd.setListener(object : MaxAdListener {
//            override fun onAdLoaded(p0: MaxAd) {
//                showLogD("loadCoreAd onAdLoaded")
//                objectAd = createDto(showPriority, interstitialAd, idAds)
//                handleTimeout?.onLoaded(this@InterstitialMax, coroutineScope, objectAd, scriptName)
//                handleTimeout = null
//                interstitialAd.setListener(null)
//            }
//
//            override fun onAdDisplayed(p0: MaxAd) {
//
//            }
//
//            override fun onAdHidden(p0: MaxAd) {
//                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdDismiss(adNetworkName)
//                objectAd?.listener = null
//            }
//
//            override fun onAdClicked(p0: MaxAd) {
//                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdClicked(adNetworkName)
//            }
//
//            override fun onAdLoadFailed(p0: String, p1: MaxError) {
//                showLogD("loadCoreAd onAdFailedToLoad, $p0")
//                handleTimeout?.onLoadFail(this@InterstitialMax, IKAdError(p1), scriptName)
//                handleTimeout = null
//                interstitialAd.setListener(null)
//            }
//
//            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
//                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdShowFail(
//                    adNetworkName,
//                    IKAdError(p1)
//                )
//            }
//
//        })
        runCatching {
//            val awsResponse = IKApplovinHelper.getInterAws()
//            if (awsResponse != null)
//                interstitialAd.setLocalExtraParameter("amazon_ad_response", awsResponse)
//            else if (IKApplovinHelper.interAwsError != null)
//                interstitialAd.setLocalExtraParameter(
//                    "amazon_ad_error",
//                    IKApplovinHelper.interAwsError
//                )
        }
//        interstitialAd.loadAd()
        handleTimeout?.startHandle(this, scriptName)
    }

    private fun setupOnPaidEventListener(screen: String) = MaxAdRevenueListener {
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