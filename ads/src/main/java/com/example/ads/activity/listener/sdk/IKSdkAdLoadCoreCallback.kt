package com.example.ads.activity.listener.sdk

interface IKSdkAdLoadCoreCallback<T, E> {
    fun onAdLoadFail(adNetwork: String, error: E)
    fun onAdLoaded(adNetwork: String, adsResult: T?)
}