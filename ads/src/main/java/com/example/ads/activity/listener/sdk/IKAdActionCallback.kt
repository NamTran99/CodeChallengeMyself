package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError

interface IKAdActionCallback<T, E> {
    fun onAdClicked(adNetwork: String)
    fun onAdImpression(adNetwork: String)
    fun onAdRewarded(adNetwork: String) {}
    fun onAdShowed(adNetwork: String) {}
    fun onAdShowFail(adNetwork: String, error: IKAdError) {}
    fun onAdDismiss(adNetwork: String) {}
    fun onAdImpression(impressionData: Any?) {}
    fun onAdImpression(adUnit: String, impressionData: Any?) {}
}