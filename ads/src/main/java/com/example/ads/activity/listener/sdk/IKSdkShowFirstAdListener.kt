package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError


interface IKSdkShowFirstAdListener {
    fun onAdsShowed(priority: Int, format: String)
    fun onAdsDismiss()
    fun onAdsShowFail(error: IKAdError)
}