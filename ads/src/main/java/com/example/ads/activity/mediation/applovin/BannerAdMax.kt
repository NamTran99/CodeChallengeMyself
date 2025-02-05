package com.example.ads.activity.mediation.applovin

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class BannerAdMax :
    IKSdkBaseBannerAd<MaxAdView>(AdNetwork.AD_MAX) {

    override fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.BANNER)
            if (adReady?.loadedAd != null) {
                showAdWithAdObject(adReady, screen, scriptName, showAdListener)
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
        adReady: IKSdkBaseLoadedAd<MaxAdView>,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        adReady.loadedAd?.setRevenueListener(
            setupOnPaidEventListener(
                screen,
                scriptName,
                showAdListener
            )
        )
        showLogD("showAdWithAdObject start show")
        adReady.listener = object : IKAdActionCallback<MaxAdView, Any> {
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

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<MaxAdView>
    ) {
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            callback.onAdFailedToLoad(
                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
            )
            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
            return
        }
        withContext(Dispatchers.Default) {
            showLogD("loadCoreAd pre start")
            val unitId = idAds.adUnitId?.trim()
            if (unitId.isNullOrBlank()) {
                callback.onAdFailedToLoad(
                    adNetworkName,
                    IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD)
                )
                showLogD("loadCoreAd unit empty")
                return@withContext
            }
            if (!isLoadAndShow && checkLoadSameAd(
                    idAds.adPriority ?: 0,
                    idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.BANNER
                )
            ) {
                callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
                showLogD("loadCoreAd an ad ready")
                return@withContext
            }
            showLogD("loadCoreAd start")
            var handleTimeout: IKSdkHandleTimeoutAd<MaxAdView>? =
                IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)

//            val adView = MaxAdView(unitId, context)
            val adFormat: MaxAdFormat
//            val isTablet = AppLovinSdkUtils.isTablet(context)
            // Get the adaptive banner height.
//            val heightDp = MaxAdFormat.BANNER.getAdaptiveSize(context).height
//            val heightPx = AppLovinSdkUtils.dpToPx(context, heightDp)
//            adFormat = if (isTablet) MaxAdFormat.LEADER else MaxAdFormat.BANNER
//            adView.layoutParams =
//                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
//            adView.gravity = Gravity.CENTER
//            adView.setExtraParameter("adaptive_banner", "true")
            var objectAd: IKSdkBaseLoadedAd<MaxAdView>? = null
            var loadCoreCallback: IKSdkLoadCoreAdCallback<MaxAdView>? =
                object : IKSdkLoadCoreAdCallback<MaxAdView> {
                    override fun onLoaded(result: MaxAdView) {
                        objectAd = createDto(showPriority, result, idAds)
                        handleTimeout?.onLoaded(
                            this@BannerAdMax,
                            coroutineScope,
                            objectAd,
                            scriptName
                        )
                        showLogD("loadCoreAd onAdLoaded")
                        handleTimeout = null
                    }

                    override fun onLoadFail(error: IKAdError) {
                        showLogD("loadCoreAd onAdFailedToLoad, $error")
                        handleTimeout?.onLoadFail(this@BannerAdMax, error, scriptName)
                        handleTimeout = null
                    }
                }
//            adView.setListener(object : MaxAdViewAdListener {
//                override fun onAdLoaded(ad: MaxAd) {
//                    loadCoreCallback?.onLoaded(adView)
//                    loadCoreCallback = null
//                }
//
//                override fun onAdDisplayed(ad: MaxAd) {
//                }
//
//                override fun onAdHidden(ad: MaxAd) {
//                    showLogD("loadCoreAd onAdHidden")
//                    kotlin.runCatching {
//                        adView.destroy()
//                    }
//                }
//
//                override fun onAdClicked(ad: MaxAd) {
//                    showLogD("loadCoreAd onAdClicked")
//                    objectAd?.listener?.onAdClicked(adNetworkName)
//                }
//
//                override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
//                    loadCoreCallback?.onLoadFail(IKAdError(error))
//                    loadCoreCallback = null
//                }
//
//                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
//                    callback.onAdFailedToLoad(adNetworkName, IKAdError(error))
//                    objectAd?.listener?.onAdShowFail(adNetworkName, IKAdError(error))
//                    kotlin.runCatching {
//                        adView.destroy()
//                    }
//                }
//
//                override fun onAdExpanded(ad: MaxAd) {
//                }
//
//                override fun onAdCollapsed(ad: MaxAd) {
//                }
//
//            })
//            runCatching {
//                val awsResponse = IKApplovinHelper.getBannerAws(adFormat.size)
//                if (awsResponse != null)
//                    adView.setLocalExtraParameter("amazon_ad_response", awsResponse)
//                else if (IKApplovinHelper.bannerAwsError != null)
//                    adView.setLocalExtraParameter(
//                        "amazon_ad_error",
//                        IKApplovinHelper.bannerAwsError
//                    )
//            }
//            adView.loadAd()
            handleTimeout?.startHandle(this@BannerAdMax, scriptName)
        }
    }

    private fun setupOnPaidEventListener(
        screen: String, scriptName: String,
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