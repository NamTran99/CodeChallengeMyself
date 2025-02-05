package com.example.ads.activity.mediation.applovin

import android.app.Activity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.open_ads.IKSdkBaseAppOpenAd
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

class AppOpenMax : IKSdkBaseAppOpenAd<MaxAppOpenAd>(AdNetwork.AD_MAX) {
    private var mCurrentCallbackObj: IKAdActionCallback<*, *>? = null
    override suspend fun showAd(
        coroutineScope: CoroutineScope,
        activity: Activity,
        screen: String,
        adsListener: IKSdkBaseListener
    ) {
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.OPEN)
            if (adReady?.loadedAd == null) {
                adsListener.onAdShowFailed(
                    adNetworkName,
                    screen,
                    IKSdkDefConst.TXT_SCRIPT_SHOW,
                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
                )
                showLogD("not valid Ad")
                return@launchWithSupervisorJob
            }
            adReady.loadedAd?.setRevenueListener(setupOnPaidEventListener(screen))
            showLogD("showAd start show")
            var adDismiss: (() -> Unit)? = {
                isAdShowing = false
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
                adReady.loadedAd?.destroy()
                adReady.loadedAd?.setListener(null)
                adReady.loadedAd?.setRevenueListener(null)
                mCurrentCallbackObj = null
                adReady.loadedAd = null
                adsListener.onAdShowFailed(
                    adNetworkName,
                    screen,
                    IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
                    p1 ?: IKAdError(
                        IKSdkErrorCode.SHOWING_FAIL.code,
                        adNetworkName + " " + IKSdkErrorCode.SHOWING_FAIL.message,
                    )
                )
            }
            adReady.loadedAd?.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {

                }

                override fun onAdDisplayed(p0: MaxAd) {
                    isAdShowing = true
                    showLogD("showAd onAdDisplayed")
                    adsListener.onAdShowed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
                        adReady.adPriority, adReady.uuid
                    )
                    showLogD("showAd OnAdImpression")
                    adsListener.onAdImpression(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adNetwork + "_" + adReady.adPriority,
                        adReady.uuid
                    )
                }

                override fun onAdHidden(p0: MaxAd) {
                    adDismiss?.invoke()
                    adDismiss = null
                }

                override fun onAdClicked(p0: MaxAd) {
                    showLogD("showAd onAdClicked")
                    adsListener.onAdClicked(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {

                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    onShowFail?.invoke(IKAdError(p1))
                    onShowFail = null
                    showLogD("showAd onAdDisplayFailed error:$p1 ")
                }
            })
            mCurrentCallbackObj = adReady.listener
            adReady.loadedAd?.showAd()
        }
    }

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<MaxAppOpenAd>
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
        val unitId = idAds.adUnitId?.trim()
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
                idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.OPEN
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
        var handleTimeout: IKSdkHandleTimeoutAd<MaxAppOpenAd>? =
            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
        var objectAd: IKSdkBaseLoadedAd<MaxAppOpenAd>? = null
//        val appOpenAd = MaxAppOpenAd(unitId, context)
        val callbackLoad = object : MaxAdListener {

            override fun onAdLoaded(p0: MaxAd) {
                showLogD("loadCoreAd onAdLoaded")
//                objectAd = createDto(showPriority, appOpenAd, idAds)
//                handleTimeout?.onLoaded(this@AppOpenMax, coroutineScope, objectAd, scriptName)
//                handleTimeout = null
//                appOpenAd.setListener(null)
            }

            override fun onAdDisplayed(p0: MaxAd) {
                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdShowed(adNetworkName)
            }

            override fun onAdHidden(ad: MaxAd) {
//                appOpenAd.setListener(null)
//                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdDismiss(adNetworkName)
//                objectAd?.listener = null
//                appOpenAd.destroy()
            }

            override fun onAdClicked(ad: MaxAd) {
                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdClicked(adNetworkName)
            }

            override fun onAdLoadFailed(p0: String, error: MaxError) {
                showLogD("loadCoreAd onAdFailedToLoad, $p0")
                handleTimeout?.onLoadFail(this@AppOpenMax, IKAdError(error), scriptName)
                handleTimeout = null
//                appOpenAd.setListener(null)
            }

            override fun onAdDisplayFailed(p0: MaxAd, error: MaxError) {
//                appOpenAd.setListener(null)
                (objectAd?.listener ?: mCurrentCallbackObj)?.onAdShowFail(
                    adNetworkName,
                    IKAdError(error)
                )
                objectAd?.listener = null
//                appOpenAd.destroy()
            }
        }
//        appOpenAd.setListener(callbackLoad)
//        appOpenAd.loadAd()
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