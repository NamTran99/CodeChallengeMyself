package com.example.ads.activity.mediation.admob

import android.os.Bundle
import android.view.ViewGroup
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto
import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

open class BannerCollapseAdmob : IKSdkBaseBannerAd<AdView>(AdNetwork.AD_MOB) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.BANNER_COLLAPSE

    override fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {

    }

    override fun showAdWithAdObject(
        adReady: IKSdkBaseLoadedAd<AdView>,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
    }

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<AdView>
    ) {
    }

    override suspend fun loadAndShowCollapsibleAd(
        idAds: IKAdUnitDto,
        adData: IKSdkProdWidgetDetailDto,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        val scriptName: String =
            IKSdkDefConst.TXT_SCRIPT_LOAD + IKSdkDefConst.Banner.COLLAPSIBLE + "_" + adNetworkName
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            showAdListener.onAdShowFail(
            IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID), scriptName, adNetworkName)
            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
            return
        }
        showLogD("loadCoreAd pre start")
        val unitId = idAds.adUnitId?.trim()
        if (unitId.isNullOrBlank()) {
            showAdListener.onAdShowFail(
                IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID),
                scriptName,
                adNetworkName
            )
            showLogD("loadCoreAd unit empty")
            return
        }

        showLogD("loadCoreAd start")
        val startLoadTime = System.currentTimeMillis()
//        val newContext =
//            withContext(Dispatchers.Default) {
//                IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.lastOrNull()
//                    ?: context
//            }
//        val adView = AdView(newContext)
//        adView.setAdSize(
//            getAdmobAdSize(newContext, null)
//        )

//        adView.adUnitId = unitId
        val adRequest = AdRequest.Builder()
        kotlin.runCatching {
            val bundle = Bundle()
            bundle.putString(
                IKSdkDefConst.Banner.COLLAPSIBLE,
                if (adData.collapsePosition == IKSdkDefConst.Banner.TOP)
                    IKSdkDefConst.Banner.TOP else IKSdkDefConst.Banner.BOTTOM
            )
            bundle.putString(
                IKSdkDefConst.Banner.COLLAPSIBLE_REQUEST_ID,
                UUID.randomUUID().toString()
            )
            adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
        }
        val isEventHandled = AtomicBoolean(false)
        var loadCoreCallback: IKSdkLoadCoreAdCallback<AdView>? =
            object : IKSdkLoadCoreAdCallback<AdView> {
                override fun onLoaded(result: AdView) {
                    showLogD("loadCoreAd onAdLoaded")
                    if (!isEventHandled.compareAndSet(false, true))
                        return
                    kotlin.runCatching {
//                        adView.parent?.let {
//                            (it as? ViewGroup)?.removeView(adView)
//                        }
                    }
//                    val objectAd = IKSdkBaseLoadedAd(
//                        unitId,
//                        loadedAd = adView,
//                        adPriority = idAds.adPriority ?: 0,
//                        showPriority = 0
//                    ).apply {
//                        this.adNetwork = adNetworkName
//                        this.adFormat = adFormatName
//                        this.lastTimeLoaded = System.currentTimeMillis()
//                    }
//                    adView.onPaidEventListener = adView.setupOnPaidEventListener(screen)

//                    showAdListener.onAdReady(
//                        objectAd,
//                        scriptName,
//                        adNetworkName
//                    )

//                    trackAdLoaded(
//                        startLoadTime,
//                        idAds.adPriority ?: 0,
//                        unitId,
//                        scriptName,
//                        objectAd.uuid
//                    )
                }

                override fun onLoadFail(error: IKAdError) {
                    showLogD("loadCoreAd onAdFailedToLoad, $error")
                    if (!isEventHandled.compareAndSet(false, true))
                        return
                    showAdListener.onAdShowFail(
                        IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
                        scriptName,
                        adNetworkName
                    )
                    trackAdLoadFail(
                        startLoadTime, idAds.adPriority ?: 0, unitId,
                        scriptName, error.message,
                        "${error.code}"
                    )
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
//                showAdListener.onAdClick(scriptName, adNetworkName)
//            }
//
//            override fun onAdImpression() {
//                showLogD("loadCoreAd onAdImpression")
//                showAdListener.onAdImpression(
//                    scriptName,
//                    adNetworkName
//                )
//            }
//        }

        withContext(Dispatchers.Main) {
//            adView.loadAd(adRequest.build())
        }
    }

    private fun AdView?.setupOnPaidEventListener(screen: String) = OnPaidEventListener {
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