package com.example.ads.activity.mediation.applovin

interface SDKMaxAdListener {
    fun onAdClicked(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdImpression(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdLoaded(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdLoadFailed(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdDismissed(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdShowFail(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdShowed(adsName: String, trackingScreen: String, scriptName: String?)
    fun onAdStartLoad(param: String, typeAds: String, trackingScreen: String)
}