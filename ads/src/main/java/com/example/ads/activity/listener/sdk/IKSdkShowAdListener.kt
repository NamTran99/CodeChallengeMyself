package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError


interface IKSdkShowAdListener {
    fun onAdShowed(priority: Int)
    suspend fun onAdReady(priority: Int)
    fun onAdDismiss()
    fun onAdShowFail(error: IKAdError)
    fun onAdShowTimeout() {

    }
}