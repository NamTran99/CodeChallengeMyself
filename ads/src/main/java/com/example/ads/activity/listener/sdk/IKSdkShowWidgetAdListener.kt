package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd

interface IKSdkShowWidgetAdListener {
    fun onAdReady(adData: IKSdkBaseLoadedAd<*>, scriptName: String, adNetworkName:String)
    fun onAdReloaded(adData: IKSdkBaseLoadedAd<*>, scriptName: String, adNetworkName: String)
    fun onAdReloadFail(error: IKAdError, scriptName: String, adNetworkName:String)
    fun onAdShowFail(error: IKAdError, scriptName: String, adNetworkName:String)
    fun onAdClick(scriptName: String, adNetworkName:String)
    fun onAdImpression(scriptName: String, adNetworkName:String)
}