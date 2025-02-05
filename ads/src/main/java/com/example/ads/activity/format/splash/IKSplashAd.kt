package com.example.ads.activity.format.splash

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.example.ads.activity.listener.keep.IKSdkSplashAdInterface
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKNoneSplashAdListener
import com.example.ads.activity.listener.pub.IKShowAdListener
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class IKSplashAd : IKSdkSplashAdInterface {
    private var scope: CoroutineScope? = null
    private var mCallback: IKShowAdListener? = null
    private var mLoadAdCallback: IKLoadAdListener? = null

    constructor(life: Lifecycle) {
        attachLifecycle(life)
    }

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

    constructor()

    override suspend fun showSplashScreenAd(activity: Activity?, listener: IKShowAdListener?) {
        mCallback = listener
//        IKSdkSplashAdCore.showSplashScreenAd(activity, mCallback)
    }

    @Deprecated(
        message = "This function may return null unexpectedly due to the asynchronous nature of coroutines. Use showSplashScreenAd directly within your coroutine context instead.",
        replaceWith = ReplaceWith(
            expression = "CoroutineScope(Dispatchers.Main).launch { showSplashScreenAd(activity, listener) }",
            imports = arrayOf("kotlinx.coroutines.launch", "kotlinx.coroutines.Dispatchers")
        )
    )
    override fun showSplashScreenAdNor(activity: Activity?, listener: IKShowAdListener?) {
        mCallback = listener
//        IKSdkSplashAdCore.showSplashScreenAdNor(activity, mCallback)
    }

    @Deprecated(
        message = "This function may return null unexpectedly due to the asynchronous nature of coroutines. Use showSplashScreenAd directly within your coroutine context instead.",
        replaceWith = ReplaceWith(
            expression = "CoroutineScope(Dispatchers.Main).launch { showSplashScreenAd(activity, listener) }",
            imports = arrayOf("kotlinx.coroutines.launch", "kotlinx.coroutines.Dispatchers")
        )
    )
    override fun loadAndShowSplashScreenAdNonAsync(
        activity: Activity?,
        listener: IKShowAdListener?
    ) {
        mCallback = listener
//        IKSdkSplashAdCore.loadAndShowSplashScreenAdNonAsync(activity, mCallback)
    }

    override suspend fun loadAndShowSplashScreenAd(
        activity: Activity?,
        listener: IKShowAdListener?
    ): Job? {
        return null
//        mCallback = listener
//        return IKSdkSplashAdCore.loadAndShowSplashScreenAd(activity, mCallback)
    }

    override fun loadSplashScreenAd(activity: Activity?, listener: IKLoadAdListener?) {
        mLoadAdCallback = listener
//        IKSdkSplashAdCore.loadSplashScreenAd(activity, mLoadAdCallback)
    }

    override fun noneShowSplashAd(activity: Activity?, listener: IKNoneSplashAdListener) {
//        IKSdkSplashAdCore.noneShowSplashAd(activity, listener)
    }

    override fun destroy() {
        mLoadAdCallback = null
        mCallback = null
    }

}