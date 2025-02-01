package com.example.ads.activity.listener.pub

interface IKAppOpenAdCallback {
    fun onShowAdComplete()
    fun onShowAdFail()
    fun onAdDismiss()
    fun onAdLoading()
    fun onAdsShowTimeout()
}