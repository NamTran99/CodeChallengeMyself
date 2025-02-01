package com.example.ads.activity.format.intertial

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKLoadingsAdListener
import com.example.ads.activity.listener.pub.IKShowAdListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.lang.ref.WeakReference
import java.util.UUID

class IKInterstitialAd : IKInterstitialAdInterface {
    private var loadAdListener = mutableMapOf<String, WeakReference<IKLoadAdListener>>()
    private var showAdListener = mutableMapOf<String, WeakReference<IKShowAdListener>>()
    private var mLoadingCallback: IKLoadingsAdListener? = null
    private var scope: CoroutineScope? = null

    constructor(life: Lifecycle) {
        attachLifecycle(life)
    }

    constructor()

    override fun attachLifecycle(life: Lifecycle) {
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

    override fun loadAd(
        screenAd: String,
        callback: IKLoadAdListener?
    ) {
        val listID = generateAdId()
        loadAdListener[listID] = WeakReference(callback)
//        IKInterController.loadAd(screenAd, object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                loadAdListener[listID]?.get()?.onAdLoaded()
//                loadAdListener.remove(listID)
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                loadAdListener[listID]?.get()?.onAdLoadFail(error)
//                loadAdListener.remove(listID)
//            }
//        })
    }

    override fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowAdListener?
    ) {
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        val weakActivity = WeakReference(activity)
//        IKInterController.showAd(
//            weakActivity.get(),
//            screen,
//            adListener = showAdListener[listID]?.get(),
//            true
//        )
    }

    override fun showAdCustom(
        activity: Activity?,
        screen: String,
        adListener: IKShowAdListener?
    ) {
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        val weakActivity = WeakReference(activity)
//        IKInterController.showAdCustom(
//            weakActivity.get(),
//            screen,
//            adListener = showAdListener[listID]?.get(),
//            false
//        )
    }

    override fun showAd(
        activity: Activity?,
        screen: String,
        adListener: IKShowAdListener?,
        loadingCallback: IKLoadingsAdListener?
    ) {
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        mLoadingCallback = loadingCallback
        val weakActivity = WeakReference(activity)
//        IKInterController.showAd(
//            weakActivity.get(),
//            screen,
//            adListener = showAdListener[listID]?.get(),
//            true,
//            mLoadingCallback
//        )
    }

    override fun showAdBackApp(
        activity: Activity?,
        adListener: IKShowAdListener?
    ) {
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        val weakActivity = WeakReference(activity)
//        IKInterController.showAd(
//            weakActivity.get(),
//            IKSdkDefConst.AdScreen.BACK_APP,
//            adListener = showAdListener[listID]?.get(),
//            false
//        )
    }

    override fun showAdBackApp(
        activity: Activity?,
        adListener: IKShowAdListener?,
        loadingCallback: IKLoadingsAdListener?
    ) {
        val listID = generateAdId()
        showAdListener[listID] = WeakReference(adListener)
        val weakActivity = WeakReference(activity)
        mLoadingCallback = loadingCallback
//        IKInterController.showAd(
//            weakActivity.get(),
//            IKSdkDefConst.AdScreen.BACK_APP,
//            adListener = showAdListener[listID]?.get(),
//            false,
//            mLoadingCallback
//        )
    }

    override fun destroy() {
        loadAdListener.clear()
        showAdListener.clear()
        mLoadingCallback = null
    }
}