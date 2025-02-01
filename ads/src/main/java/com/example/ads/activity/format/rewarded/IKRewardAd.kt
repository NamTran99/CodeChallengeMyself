package com.example.ads.activity.format.rewarded

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKLoadingsAdListener
import com.example.ads.activity.listener.pub.IKShowRewardAdListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.ref.WeakReference
import java.util.UUID

class IKRewardAd {
    companion object {
        private const val LOG_TAG = "IKRewardAd_"
    }

    private var loadAdListener = mutableMapOf<String, WeakReference<IKLoadAdListener>>()
    private var showAdListener = mutableMapOf<String, WeakReference<IKShowRewardAdListener>>()
    private var scope: CoroutineScope? = null

    constructor(life: Lifecycle) {
        attachLifecycle(life)
    }

    constructor()

    fun attachLifecycle(life: Lifecycle) {
        scope = life.coroutineScope
        life.coroutineScope.launchWithSupervisorJob(Dispatchers.Main) {
            life.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    kotlin.runCatching {
                        life.removeObserver(this)
                    }
                    destroy()
                }
            })
        }
    }

    private fun generateAdId(): String = UUID.randomUUID().toString()
    fun loadAd(
        screen: String,
        callback: IKLoadAdListener?
    ) {
        showLogD("loadAd") { "start run" }
        val listID = generateAdId()
        loadAdListener[listID] = WeakReference(callback)
        IKRewardedController.loadAd(screen, object : IKSdkLoadAdCoreListener {
            override fun onAdLoaded() {
                loadAdListener[listID]?.get()?.onAdLoaded()
                loadAdListener.remove(listID)
                showLogD("loadAd") { "onAdLoaded" }
            }

            override fun onAdLoadFail(error: IKAdError) {
                loadAdListener[listID]?.get()?.onAdLoadFail(error)
                loadAdListener.remove(listID)
                showLogD("loadAd") { "onAdLoadFail $error" }
            }
        })
    }

    fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowRewardAdListener?
    ) {
        showLogD("showAd") { "start run f1" }
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        val weakActivity = WeakReference(activity)
        IKRewardedController.showAd(
            weakActivity.get(),
            screen,
            adListener = showAdListener[listID]?.get()
        )
    }

    fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowRewardAdListener?,
        loadingCallback: IKLoadingsAdListener? = null
    ) {
        showLogD("showAd") { "start run f2" }
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        val weakActivity = WeakReference(activity)
        IKRewardedController.showAd(
            weakActivity.get(),
            screen,
            adListener = showAdListener[listID]?.get(),
            loadingCallback
        )
    }

    fun destroy() {
        loadAdListener.clear()
        showAdListener.clear()
    }


    private fun showLogD(tag: String, message: () -> String) {
        IKLogs.d("IKRewardAd") {
            "${tag}:" + message.invoke()
        }
    }
}