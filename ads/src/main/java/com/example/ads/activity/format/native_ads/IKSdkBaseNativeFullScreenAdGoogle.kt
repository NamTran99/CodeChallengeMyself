package com.example.ads.activity.format.native_ads

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.nativead.NativeAd
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import kotlinx.coroutines.CoroutineScope


abstract class IKSdkBaseNativeFullScreenAdGoogle(adNetwork: AdNetwork) :
    IKSdkBaseNativeFullScreenAds<NativeAd>(adNetwork) {
    override fun showAdWithAdObject(
        adReady: IKSdkBaseLoadedAd<NativeAd>,
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        adReady.loadedAd?.setOnPaidEventListener(
            adReady.loadedAd?.setupOnPaidEventListener(
                adReady.unitId ?: IKSdkDefConst.UNKNOWN, screen
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
            @Suppress("UNCHECKED_CAST") val adReady =
                adObject.adObject as? IKSdkBaseLoadedAd<NativeAd>?
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
                adReady, scriptName, screen, showAdListener
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