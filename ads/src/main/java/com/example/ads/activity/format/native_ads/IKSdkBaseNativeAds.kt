package com.example.ads.activity.format.native_ads

import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.SDKAdPriorityDto
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.listener.sdk.IKSdkAdLoadCoreCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import kotlinx.coroutines.CoroutineScope


abstract class IKSdkBaseNativeAds<T : Any>(adNetwork: AdNetwork) :
    IKSdkBaseAd<T>(adNetwork) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.NATIVE
    override val logTag: String
        get() = "${adFormatName}_$adNetworkName"

    abstract fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    )

    suspend fun getReadyDisplayAd(): IkmDisplayWidgetAdView? {
        val mAdsObject = getReadyAd(IKSdkDefConst.TimeOutAd.NATIVE)
        return if (mAdsObject == null) return null
        else IkmDisplayWidgetAdView(
            mAdsObject, SDKAdPriorityDto(
                adNetwork.value,
                mAdsObject.adPriority,
                mAdsObject.showPriority
            ),
            adF = IKSdkDefConst.AdFormat.NATIVE
        )
    }

    abstract fun showAdWithAdObject(
        adReady: IKSdkBaseLoadedAd<T>,
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    )

    private fun handleAdLoadFail(
        scope: CoroutineScope,
        error: IKAdError,
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        scope.launchWithSupervisorJob {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.NATIVE)
            if (adReady?.loadedAd != null) {
                showLogD("showAd cache1")
                showAdWithAdObject(
                    adReady,
                    scriptName,
                    screen,
                    showAdListener
                )
            } else {
                showAdListener.onAdShowFail(error, scriptName, adNetworkName)
                showLogD("showAd not valid Ad1")
            }
        }
    }

    private fun handleAdLoaded(
        adsResult: IKSdkBaseLoadedAd<T>?,
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        if (adsResult != null) {
            showAdWithAdObject(
                adsResult,
                scriptName,
                screen,
                showAdListener
            )
        } else {
            showLogD("showAd not valid Ad2")
            showAdListener.onAdShowFail(
                IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
                scriptName,
                adNetworkName
            )
        }
    }

    suspend fun loadAndShowAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        unitDto: IKAdUnitDto?,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        showLogD("showAd load 1")
        val callback = object : IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError> {
            override fun onAdLoadFail(adNetwork: String, error: IKAdError) {
                handleAdLoadFail(
                    coroutineScope,
                    error,
                    scriptName,
                    screen,
                    showAdListener
                )
            }

            override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                handleAdLoaded(
                    adsResult,
                    scriptName,
                    screen,
                    showAdListener
                )
            }
        }
        loadSingleAdSdk(
            coroutineScope,
            0,
            scriptName,
            screen,
            true,
            unitDto,
            callback
        )
    }

    open fun showAdWithAdView(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        adObject: IkmDisplayWidgetAdView,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
    }

}