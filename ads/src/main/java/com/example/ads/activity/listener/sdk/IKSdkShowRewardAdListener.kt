package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError


interface IKSdkShowRewardAdListener {
    fun onAdsShowed(priority: Int)
    suspend fun onAdReady(priority: Int)
    fun onAdsDismiss()
    fun onAdsRewarded()
    fun onAdsShowFail(error: IKAdError)
}