package com.example.ads.activity.mediation.applovin

import android.app.Activity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.rewarded.IKSdkBaseRewardAd
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKAdActionCallbackObj
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class RewardedMax : IKSdkBaseRewardAd<MaxRewardedAd>(AdNetwork.AD_MAX) {
    private var mCurrentCallbackObj: IKAdActionCallback<*, *>? = null
    override fun showAd(
        coroutineScope: CoroutineScope,
        activity: Activity,
        screen: String,
        adsListener: IKSdkBaseListener
    ) {
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.REWARD)
            if (adReady?.loadedAd == null) {
                showLogD("not valid Ad")
                adsListener.onAdShowFailed(
                    adNetworkName,
                    screen,
                    IKSdkDefConst.TXT_SCRIPT_SHOW,
                    IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
                )
                return@launchWithSupervisorJob
            }
            adReady.loadedAd?.setRevenueListener(setupOnPaidEventListener(screen))
            showLogD("showAd start show")
            adReady.listener = object : IKAdActionCallback<MaxRewardedAd, Any> {
                override fun onAdClicked(adNetwork: String) {
                    adsListener.onAdClicked(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdImpression(adNetwork: String) {
                    adsListener.onAdImpression(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdRewarded(adNetwork: String) {
                    adsListener.onAdsRewarded(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW, adReady.uuid
                    )
                }

                override fun onAdDismiss(adNetwork: String) {
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
                    adReady.destroyObject()
                }

                override fun onAdShowed(adNetwork: String) {
                    isAdShowing = true
                    showLogD("onAdDisplayed")
                    adsListener.onAdShowed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority,
                        adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdShowFail(adNetwork: String, error: IKAdError) {
                    isAdShowing = false
                    adsListener.onAdShowFailed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority,
                        error
                    )
                    adReady.loadedAd?.destroy()
                    adReady.loadedAd?.setListener(null)
                    adReady.loadedAd?.setRevenueListener(null)
                    mCurrentCallbackObj = null
                    adReady.destroyObject()
                    showLogD("showAd onAdDisplayFailed error")
                }
            }
            adReady.listener2?.callbackObj = adReady.listener
            mCurrentCallbackObj = adReady.listener
            adReady.loadedAd?.showAd(activity)
            removeAdAny(adReady)
        }
    }

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<MaxRewardedAd>
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
            callback.onAdFailedToLoad(
                adNetworkName,
                IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID)
            )
            showLogD("loadCoreAd unit empty")
            return
        }
        if (checkLoadSameAd(
                idAds.adPriority ?: 0,
                idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.REWARD
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
        var handleTimeout: IKSdkHandleTimeoutAd<MaxRewardedAd>? =
            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
        var objectAd: IKSdkBaseLoadedAd<MaxRewardedAd>?
//        val rewardedAd = MaxRewardedAd.getInstance(unitId, context)

        val listenerAd2: IKAdActionCallbackObj<MaxRewardedAd, Any> =
            object : IKAdActionCallbackObj<MaxRewardedAd, Any>() {

                override fun onAdClicked(adNetwork: String) {
                    (callbackObj ?: mCurrentCallbackObj)?.onAdClicked(adNetworkName)
                }

                override fun onAdImpression(adNetwork: String) {
                    (callbackObj ?: mCurrentCallbackObj)?.onAdImpression(adNetworkName)
                }

                override fun onAdRewarded(adNetwork: String) {
                    (callbackObj ?: mCurrentCallbackObj)?.onAdRewarded(adNetworkName)
                }

                override fun onAdShowed(adNetwork: String) {
                    (callbackObj ?: mCurrentCallbackObj)?.onAdShowed(adNetworkName)
                }

                override fun onAdShowFail(adNetwork: String, error: Any) {
                    (callbackObj ?: mCurrentCallbackObj)?.onAdShowFail(
                        adNetworkName, (error as? IKAdError) ?: IKAdError(
                            IKSdkErrorCode.SHOWING_FAIL.code,
                            adNetworkName + " " + IKSdkErrorCode.SHOWING_FAIL.message,
                        )
                    )
                }

                override fun onAdDismiss(adNetwork: String) {
                    (callbackObj ?: mCurrentCallbackObj)?.onAdDismiss(adNetworkName)
                }
            }
        val listener = object : MaxRewardedAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                showLogD("loadCoreAd onAdLoaded")
//                objectAd = createDto(showPriority, rewardedAd, idAds)
//                objectAd?.listener2 = listenerAd2
//                handleTimeout?.onLoaded(this@RewardedMax, coroutineScope, objectAd, scriptName)
                handleTimeout = null
            }

            override fun onAdDisplayed(p0: MaxAd) {
                listenerAd2.onAdShowed(adNetworkName)
            }

            override fun onAdHidden(p0: MaxAd) {
                listenerAd2.onAdDismiss(adNetworkName)
//                rewardedAd.setListener(null)
            }

            override fun onAdClicked(p0: MaxAd) {
                listenerAd2.onAdClicked(adNetworkName)
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                showLogD("loadCoreAd onAdFailedToLoad, $p0")
                handleTimeout?.onLoadFail(this@RewardedMax, IKAdError(p1), scriptName)
                handleTimeout = null
//                rewardedAd.setListener(null)
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
//                rewardedAd.setListener(null)
                listenerAd2.onAdShowFail(adNetworkName, p1)
            }

            override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                listenerAd2.onAdRewarded(adNetworkName)
            }

        }
//        rewardedAd.setListener(listener)
//
//        rewardedAd.loadAd()
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