package com.example.ads.activity.listener.sdk

abstract class IKAdActionCallbackObj<T, E> {
    var callbackObj: IKAdActionCallback<T, E>? = null
    abstract fun onAdClicked(adNetwork: String)
    abstract fun onAdImpression(adNetwork: String)
    open fun onAdRewarded(adNetwork: String) {}
    open fun onAdShowed(adNetwork: String) {}
    open fun onAdShowFail(adNetwork: String, error: E) {}
    open fun onAdDismiss(adNetwork: String) {}
}