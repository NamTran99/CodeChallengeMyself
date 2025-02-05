package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError

abstract class IKSdkBaseListener {
    abstract fun onAdClicked(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    )

    abstract fun onAdImpression(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    )

    abstract fun onAdShowed(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        priority: Int,
        adUUID: String
    )

    abstract fun onAdShowFailed(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        error: IKAdError
    )

    abstract fun onAdDismissed(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    )

    open fun onAdsRewarded(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    ) {
    }
}