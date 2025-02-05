package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd

interface IKSdkLoadDisplayAdViewListener {
    fun onAdLoaded(adObject: IKSdkBaseLoadedAd<*>?)
    fun onAdLoadFail(error: IKAdError)
}