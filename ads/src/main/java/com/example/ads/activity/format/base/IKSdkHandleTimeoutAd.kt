package com.example.ads.activity.format.base

import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdSizeDto
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class IKSdkHandleTimeoutAd<T>(
    val adNetworkName: String,
    val idAds: IKAdUnitDto,
    val callback: IKSdkAdCallback<T>
) {
    private val startLoadTime = System.currentTimeMillis()
    private var checkLoaded = false
    private var checkFail = false
    private var loadAdJob: Job? = null
    private var loadAdScope: CoroutineScope? = null
    private var listenerTimeout: IKSdkAdCallback<T>? = null
    private var enableTimeout = false
    private val loadTimeOut = idAds.timeOut ?: 0
    private var isAdLoadDone = false
    private var isAdTimeOut = false
    private val isEventHandled = AtomicBoolean(false)

    init {
        if (loadTimeOut >= IKSdkDefConst.TimeOutAd.LOAD_WIDGET_AD_TIME_OUT) {
            loadAdJob = Job()
            loadAdScope = CoroutineScope(Dispatchers.Main + loadAdJob!!)
            listenerTimeout = callback
            enableTimeout = true
        }
    }

    fun onLoadFail(ikSdkBaseAd: IKSdkBaseAd<*>, error: IKAdError, scriptName: String) {
        this@IKSdkHandleTimeoutAd.isAdLoadDone = true

        if (!isEventHandled.compareAndSet(false, true))
            return
        if (checkFail)
            return
        val unitId = idAds.adUnitId?.trim() ?: ""
        if (error != IKAdError(IKSdkErrorCode.LOADING_AD_TIMEOUT)) {
            this@IKSdkHandleTimeoutAd.loadAdJob?.cancel()
            this@IKSdkHandleTimeoutAd.loadAdScope?.cancel()
        }
        callback.onAdFailedToLoad(adNetworkName, error)
        ikSdkBaseAd.trackAdLoadFail(
            this.startLoadTime, this.idAds.adPriority ?: 0, unitId,
            scriptName, error.message,
            "${error.code}",
            isTimeout = isAdTimeOut
        )
        checkFail = true
    }

    fun onLoaded(
        ikSdkBaseAd: IKSdkBaseAd<*>,
        coroutineScope: CoroutineScope,
        data: IKSdkBaseLoadedAd<T>?,
        scriptName: String,
        adSizeDto : IKAdSizeDto? = null
    ) {
        this@IKSdkHandleTimeoutAd.isAdLoadDone = true
        if (!isEventHandled.compareAndSet(false, true))
            return
        if (checkLoaded)
            return
        val unitId = idAds.adUnitId?.trim() ?: ""
        // add adSizeDto to check ad size when show banner inline ad
        if (adSizeDto != null) {
            data?.adSizeDto = adSizeDto
        }
        listenerTimeout = null
        if (isAdTimeOut) {
            coroutineScope.launchWithSupervisorJob {
                ikSdkBaseAd.addItemAdsData(data)
            }
        } else {
            callback.onAdLoaded(adNetworkName, data)
        }
        ikSdkBaseAd.trackAdLoaded(
            this.startLoadTime,
            this.idAds.adPriority ?: 0,
            unitId,
            scriptName,
            isTimeout = isAdTimeOut
        )
        checkLoaded = true
        this@IKSdkHandleTimeoutAd.loadAdJob?.cancel()
        this@IKSdkHandleTimeoutAd.loadAdScope?.cancel()
    }

    fun startHandle(ikSdkBaseAd: IKSdkBaseAd<*>, scriptName: String) {
        loadAdScope?.launchWithSupervisorJob(Dispatchers.Main) {
            val unitId = idAds.adUnitId?.trim() ?: ""
            delay(loadTimeOut)
            synchronized(this) {
                if (!isAdLoadDone) {
                    isAdTimeOut = true
                    listenerTimeout?.onAdFailedToLoad(
                        adNetworkName,
                        IKAdError(IKSdkErrorCode.LOADING_AD_TIMEOUT)
                    )
//                    ikSdkBaseAd.trackAdLoadFail(
//                        this@IKSdkHandleTimeoutAd.startLoadTime,
//                        this@IKSdkHandleTimeoutAd.idAds.adPriority ?: 0,
//                        unitId,
//                        scriptName,
//                        IKSdkErrorCode.LOADING_AD_TIMEOUT.message,
//                        "${IKSdkErrorCode.LOADING_AD_TIMEOUT.code}"
//                    )
                    listenerTimeout = null
                }
            }
        }
    }
}