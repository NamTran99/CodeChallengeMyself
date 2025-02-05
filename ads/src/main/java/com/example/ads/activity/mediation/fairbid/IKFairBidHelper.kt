package com.example.ads.activity.mediation.fairbid

import com.fyber.FairBid
import com.fyber.fairbid.ads.FairBidListener
import com.fyber.fairbid.ads.mediation.MediatedNetwork
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object IKFairBidHelper {
    var initStatus = false
        private set
    private val isInitializing = AtomicBoolean()
    private const val TAG = "IKFairBidHelper"
    private var mAppKey: String? = null
    fun initialize(appKey: String) {
        mAppKey = appKey
        IKLogs.dNone(TAG) { "start init" }
        if (initStatus)
            return
        if (!isInitializing.compareAndSet(false, true))
            return
        if (!hasLib()) {
            isInitializing.set(false)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            var activity =
                IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
            if (activity == null) {
                delay(1000)
                activity =
                    IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
            }
            if (activity == null) {
                delay(1000)
                activity =
                    IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
            }
            if (activity == null) {
                isInitializing.set(false)
                return@launch
            }
            kotlin.runCatching {
                IKLogs.dNone(TAG) { "run init" }
                FairBid.configureForAppId(appKey).withFairBidListener(object : FairBidListener {
                    override fun mediationFailedToStart(errorMessage: String, errorCode: Int) {
                        isInitializing.set(false)
                        IKLogs.dNone(TAG) { "start mediationFailedToStart,$errorMessage" }
                    }

                    override fun mediationStarted() {
                        isInitializing.set(false)
                        initStatus = true
                        initStatus = true
                        IKLogs.dNone(TAG) { "start mediationStarted" }
                    }

                    override fun onNetworkFailedToStart(
                        network: MediatedNetwork,
                        errorMessage: String
                    ) {
                    }

                    override fun onNetworkStarted(network: MediatedNetwork) {
                    }

                }).start(activity)
            }.onFailure {
                isInitializing.set(false)
                IKLogs.dNone(TAG) { "init fail:${it.message}" }
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(30000)
                isInitializing.set(false)
            }
        }
    }

    fun hasLib(): Boolean {
        return kotlin.runCatching {
            Class.forName(com.fyber.FairBid::class.java.name)
            IKLogs.d(TAG) { "has Init" }
            true
        }.onFailure {
            IKLogs.d(TAG) { "not Init" }
        }.getOrDefault(false)
    }

    suspend fun isInitialized(): Boolean {
        if (!hasLib())
            return false
        if (!initStatus) {
            if (isInitializing.get())
                delay(1000)
            if (isInitializing.get())
                delay(500)
            if (isInitializing.get())
                delay(500)
            if (!initStatus)
                mAppKey?.let {
                    initialize(it)
                }
        }
        return initStatus
    }


}