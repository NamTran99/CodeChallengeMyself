package com.example.ads.activity.mediation.gam

import android.app.Activity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.rewarded.IKSdkBaseRewardAd
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RewardGam : IKSdkBaseRewardAd<RewardedAd>(AdNetwork.AD_MANAGER) {

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
            adReady.loadedAd?.onPaidEventListener =
                setupOnPaidEventListener(adReady.loadedAd, screen)
            showLogD("showAd start show")
            adReady.loadedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    isAdShowing = false
                    adsListener.onAdShowFailed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority,
                        IKAdError(p0)
                    )
                    showLogD("showAd onAdFailedToShowFullScreenContent error:$p0 ")
                }

                override fun onAdShowedFullScreenContent() {
                    isAdShowing = true
                    showLogD("showAdOnAdShowedFullScreenContent")
                    adsListener.onAdShowed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority,
                        adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdDismissedFullScreenContent() {
                    isAdShowing = false
                    showLogD("showAdOnAdDismissedFullScreenContent")
                    adsListener.onAdDismissed(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                    adReady.loadedAd?.fullScreenContentCallback = null
                    adReady.loadedAd?.onPaidEventListener = null
                }

                override fun onAdImpression() {
                    showLogD("showAdOnAdImpression")
                    adsListener.onAdImpression(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    showLogD("showAdOnAdClicked")
                    adsListener.onAdClicked(
                        adNetworkName,
                        screen,
                        IKSdkDefConst.TXT_SCRIPT_SHOW + "_" + adReady.adPriority, adReady.uuid
                    )
                }
            }
            adReady.loadedAd?.show(activity) {
                adsListener.onAdsRewarded(
                    adNetworkName,
                    screen,
                    IKSdkDefConst.TXT_SCRIPT_SHOW, adReady.uuid
                )
            }
        }
    }

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<RewardedAd>
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
        var handleTimeout: IKSdkHandleTimeoutAd<RewardedAd>? =
            IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
        var objectAd: IKSdkBaseLoadedAd<RewardedAd>? = null

        val callbackLoad = object : RewardedAdLoadCallback() {

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                showLogD("loadCoreAd onAdFailedToLoad, $p0")
                handleTimeout?.onLoadFail(this@RewardGam, IKAdError(p0), scriptName)
                handleTimeout = null
            }

            override fun onAdLoaded(p0: RewardedAd) {
                super.onAdLoaded(p0)
                showLogD("loadCoreAd onAdLoaded")
                objectAd = createDto(showPriority, p0, idAds)
                handleTimeout?.onLoaded(this@RewardGam, coroutineScope, objectAd, scriptName)
                handleTimeout = null
            }
        }
        val request: AdManagerAdRequest = AdManagerAdRequest.Builder().build()
        withContext(Dispatchers.Main) {
//            RewardedAd.load(
//                context,
//                unitId,
//                request,
//                callbackLoad
//            )
        }
        handleTimeout?.startHandle(this, scriptName)
    }

    private fun setupOnPaidEventListener(
        adsObject: RewardedAd?, screen: String
    ) = OnPaidEventListener {
        val adRevenue = AdjustAdRevenue(IKSdkDefConst.Adjust.ADMOB_SDK)
        adRevenue.setRevenue(
            it.valueMicros / IKSdkDefConst.DATA_RV_D_DIV_USD, it.currencyCode
        )

        IKSdkTrackingHelper.customPaidAd(
            adNetwork = adNetworkName,
            revMicros = it.valueMicros.toDouble() / IKSdkDefConst.DATA_RV_D_DIV_USD,
            currency = it.currencyCode,
            adUnitId = adsObject?.adUnitId ?: IKSdkDefConst.UNKNOWN,
            responseAdNetwork = adsObject?.responseInfo?.mediationAdapterClassName
                ?: IKSdkDefConst.UNKNOWN,
            adFormat = adFormatName,
            screen = screen
        )
        adRevenue.adRevenuePlacement = adFormatName
        adRevenue.adRevenueUnit = adsObject?.adUnitId ?: IKSdkDefConst.UNKNOWN
        Adjust.trackAdRevenue(adRevenue)
    }
}