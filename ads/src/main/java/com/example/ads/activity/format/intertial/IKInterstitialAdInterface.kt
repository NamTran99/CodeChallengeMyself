package com.example.ads.activity.format.intertial

import android.app.Activity
import androidx.lifecycle.Lifecycle
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKLoadingsAdListener
import com.example.ads.activity.listener.pub.IKShowAdListener

interface IKInterstitialAdInterface {
    fun attachLifecycle(life: Lifecycle)
    fun loadAd(
        screenAd: String,
        callback: IKLoadAdListener?
    )
    fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowAdListener?
    )
    fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowAdListener?,
        loadingCallback: IKLoadingsAdListener? = null
    )
    fun showAdBackApp(
        activity: Activity?,
        adListener: IKShowAdListener?
    )
    fun showAdBackApp(
        activity: Activity?,
        adListener: IKShowAdListener?,
        loadingCallback: IKLoadingsAdListener? = null
    )
    fun showAdCustom(
        activity: Activity?,
        screen: String,
        adListener: IKShowAdListener?
    )
    fun destroy()
}