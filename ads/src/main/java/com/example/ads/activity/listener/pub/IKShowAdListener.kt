package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKAdError


interface IKShowAdListener {
    fun onAdsShowed() {}
    fun onAdsDismiss()
    fun onAdsShowFail(error: IKAdError)
    fun onAdsShowTimeout() {}
}