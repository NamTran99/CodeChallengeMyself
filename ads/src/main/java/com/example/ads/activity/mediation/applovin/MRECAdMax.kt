package com.example.ads.activity.mediation.applovin

import android.content.Context
import android.view.Gravity
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
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdSizeDto
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.mrec.IKSdkBaseMRECAd
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

class MRECAdMax :
    IKSdkBaseMRECAd<MaxAdView>(AdNetwork.AD_MAX) {

    val mRepository: IKDataRepository? by lazy {
        IKDataRepository.getInstance()
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
//        val context = IKSdkApplicationProvider.getContext()
//        if (context == null) {
//            callback.onAdFailedToLoad(
//                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
//            )
//            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
//            return
//        }
        withContext(Dispatchers.IO) {
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
                    idAds.cacheSize ?: 0, IKSdkDefConst.TimeOutAd.NATIVE
                )
            ) {
                callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
                showLogD("loadCoreAd an ad ready")
                return@withContext
            }
            showLogD("loadCoreAd start")

//            val adView = MaxAdView(unitId, MaxAdFormat.MREC, context)

//            val sizeAd = if (screen.isNullOrBlank())
//                oldAdSize ?: getAdSize(context.applicationContext, screen ?: "")
//            else
//                getAdSize(context.applicationContext, screen)
//            if (oldAdSize == null)
//                getAdSize(context.applicationContext, screen ?: "")
//            adView.layoutParams = FrameLayout.LayoutParams(sizeAd.first, sizeAd.second)
//            adView.gravity = Gravity.CENTER
//            adView.setExtraParameter("adaptive_banner", "true")

            var handleTimeout: IKSdkHandleTimeoutAd<MaxAdView>? =
                IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
            var objectAd: IKSdkBaseLoadedAd<MaxAdView>? = null
            var loadCoreCallback: IKSdkLoadCoreAdCallback<MaxAdView>? =
                object : IKSdkLoadCoreAdCallback<MaxAdView> {
                    override fun onLoaded(result: MaxAdView) {
                        showLogD("loadCoreAd onAdLoaded")
                        objectAd = createDto(showPriority, result, idAds)
                        handleTimeout?.onLoaded(
                            this@MRECAdMax,
                            coroutineScope,
                            objectAd,
                            scriptName
                        )
                        handleTimeout = null
                    }

                    override fun onLoadFail(error: IKAdError) {
                        showLogD("loadCoreAd onAdFailedToLoad, $error")
                        handleTimeout?.onLoadFail(this@MRECAdMax, error, scriptName)
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

//            adView.loadAd()
            handleTimeout?.startHandle(this@MRECAdMax, scriptName)
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
        adRevenue.adRevenueUnit = it?.adUnitId ?: IKSdkDefConst.UNKNOWN
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

    private var oldAdSize: Pair<Int, Int>? = null
    suspend fun getAdSize(
        context: Context,
        screen: String?,
    ): Pair<Int, Int> {
        val adSizeConfig: IKAdSizeDto =
            getBannerInlineAdSize(screen) ?: return getAdSize(context)

        try {
            when (adSizeConfig.adType) {
                IKSdkDefConst.AdSize.NORMAL -> {
                    return getAdSize(context)
                }

                IKSdkDefConst.AdSize.ADAPTIVE -> {
                    return getAdSize(context)
                }

//                IKSdkDefConst.AdSize.VIEW_SIZE -> {
//                    val viewWidth = viewSize?.width ?: 0
//                    val viewHeight = viewSize?.height ?: 0
//                    if (viewWidth == 0 || viewHeight == 0)
//                        return getAdSize(context)
//                    return getAdSize(context, viewWidth, viewHeight)
//                }
//
//                IKSdkDefConst.AdSize.VIEW_WIDTH -> {
//                    val viewWidth = viewSize?.width ?: 0
//                    if (viewWidth == 0)
//                        return getAdSize(context)
//                    return getAdSize(context, viewWidth)
//                }

                IKSdkDefConst.AdSize.MANUAL -> {
                    val width = adSizeConfig.width ?: 0
                    val height = adSizeConfig.height ?: 0
                    if (width <= 0 || height <= 0)
                        return getAdSize(context)
                    val viewWidth = AppLovinSdkUtils.dpToPx(context, width)
                    val viewHeight = AppLovinSdkUtils.dpToPx(context, height)
                    return getAdSize(context, viewWidth, viewHeight)
                }

                IKSdkDefConst.AdSize.MANUAL_HEIGHT -> {
                    val height = adSizeConfig.height ?: 0
                    if (height <= 0)
                        return getAdSize(context)
                    return kotlin.runCatching {
                        val (adWidthPixels, _) = calculatorAdSize(context)
                        getAdSize(context, adWidthPixels, height)
                    }.getOrNull() ?: getAdSize(context)
                }
//
//                IKSdkDefConst.AdSize.MANUAL_HEIGHT_WITH_AD_WIDTH -> {
//                    val height = adSizeConfig.height ?: 0
//                    val viewWidth = viewSize?.width ?: 0
//                    if (height == 0 || viewWidth == 0)
//                        return getAdSize(context)
//                    return kotlin.runCatching {
//                        val viewHeight = AppLovinSdkUtils.dpToPx(context, height)
//                        getAdSize(context, viewWidth, viewHeight)
//                    }.getOrNull() ?: getAdSize(context)
//                }

                IKSdkDefConst.AdSize.MANUAL_WIDTH -> {
                    val width = adSizeConfig.width ?: 0
                    if (width == 0)
                        return getAdSize(context)
                    return kotlin.runCatching {
                        val (adWidthPixels, _) = calculatorAdSize(context)
                        getAdSize(context, adWidthPixels)
                    }.getOrNull() ?: getAdSize(context)
                }
            }
        } catch (e: Exception) {
            return getAdSize(context)
        }
        return getAdSize(context)
    }

    private fun getAdSize(
        context: Context,
        widthPxl: Int = 0,
        heightPxl: Int = 0
    ): Pair<Int, Int> {
        val widthPx = if (widthPxl <= 0) AppLovinSdkUtils.dpToPx(context, 320) else widthPxl
        val heightPx = if (heightPxl <= 0) AppLovinSdkUtils.dpToPx(context, 250) else heightPxl
        return Pair(widthPx, heightPx)
    }

    private suspend fun getBannerInlineAdSize(screen: String?): IKAdSizeDto? {
        return if (screen == null) {
            null
        } else {
            IKDataRepository.getConfigWidget(screen)?.adSize
        }
    }
}