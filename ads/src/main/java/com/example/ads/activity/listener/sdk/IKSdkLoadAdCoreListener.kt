package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError

interface IKSdkLoadAdCoreListener {
    fun onAdLoaded()
    fun onAdLoadFail(error: IKAdError)
}