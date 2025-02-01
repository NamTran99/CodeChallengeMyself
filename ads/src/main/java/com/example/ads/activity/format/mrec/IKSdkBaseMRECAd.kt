package com.example.ads.activity.format.mrec

import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.format.banner.IKSdkBaseBannerAd
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.utils.IKSdkDefConst


abstract class IKSdkBaseMRECAd<T : Any>(adNetwork: AdNetwork) :
    IKSdkBaseBannerAd<T>(adNetwork) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.BANNER_INLINE
    override val logTag: String
        get() = "${adFormatName}_$adNetworkName"

    private suspend fun handleAdLoadFail(
        adNetwork: String,
        error: IKAdError,
        adsListener: IKSdkBaseListener,
        showAdListener: IKSdkShowWidgetAdListener,
        scriptName: String,
        screen: String
    ) {

        val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.BANNER)
        if (adReady?.loadedAd != null) {
            showLogD("showAd cache1")

            showAdWithAdObject(
                adReady,
                screen,
                scriptName,
                showAdListener
            )
        } else {
            adsListener.onAdShowFailed(
                adNetwork,
                screen,
                scriptName,
                error
            )
            showAdListener.onAdShowFail(error, scriptName, adNetworkName)
            showLogD("showAd not valid Ad1")
        }
    }
}