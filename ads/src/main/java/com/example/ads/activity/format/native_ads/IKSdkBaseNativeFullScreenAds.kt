package com.example.ads.activity.format.native_ads

import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


abstract class IKSdkBaseNativeFullScreenAds<T : Any>(adNetwork: AdNetwork) :
    IKSdkBaseNativeAds<T>(adNetwork) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.NATIVE_FULL
    override val logTag: String
        get() = "${adFormatName}_$adNetworkName"

    override fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.NATIVE)
            if (adReady?.loadedAd != null) {
                showLogD("showAd cache1")
                showAdWithAdObject(
                    adReady,
                    scriptName,
                    screen,
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

}