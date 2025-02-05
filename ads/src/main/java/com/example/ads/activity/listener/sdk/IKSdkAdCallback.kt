package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd

interface IKSdkAdCallback<T> {
    fun onAdFailedToLoad(adNetwork: String, error: IKAdError)
    fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?)
}