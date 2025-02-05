package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKAdError


interface IKShowRewardAdListener {
    fun onAdsShowed() {}
    fun onAdsDismiss()
    fun onAdsRewarded()
    fun onAdsShowFail(error: IKAdError)
    fun onAdsShowTimeout() {}
}