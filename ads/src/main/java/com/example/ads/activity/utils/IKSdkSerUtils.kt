package com.example.ads.activity.utils

import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.mediation.fairbid.IKFairBidHelper
//import com.example.ads.activity.mediation.playgap.IKPlayGapHelper
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

object IKSdkSerUtils {
    fun timerFlow(
        timeOutStart: Long,
        period: Long,
        isActive: MutableStateFlow<Boolean>
    ): Flow<Long> {
        return flow {
            var time = 0L
            while (isActive.value && time <= timeOutStart) {
                emit(time)
                delay(period)
                time += period
            }
        }
    }

    fun startTimer(
        timeOutStart: Long,
        period: Long = 1000L,
        scope: CoroutineScope,
        onTick: suspend (Long) -> Unit,  // Callback cho mỗi tick
        onFinish: suspend () -> Unit     // Callback khi timer hoàn thành
    ): Job {
        val isActive = MutableStateFlow(true)
        var isFinished = false // Cờ kiểm tra chỉ gọi onFinish một lần
        return scope.launchWithSupervisorJob {
            timerFlow(timeOutStart, period, isActive).collect { currentTime ->
                if (currentTime < timeOutStart) {
                    onTick(currentTime)  // Handle tick
                } else {
                    if (!isFinished) {
                        onFinish() // Gọi onFinish lần đầu
                        isFinished = true // Đánh dấu là đã hoàn thành
                    }
                    isActive.value = false // Cancel this coroutine to stop the flow collection
                }
            }
        }
    }

    fun IKAdapterDto.initMediation() {
        when (this.adNetwork) {
//            AdNetwork.AD_MAX.value -> {
//                IKBaseApplication.context()
//                    ?.let { it1 ->
//                        IKApplovinHelper.initialize(it1)
//                    }
//            }

            AdNetwork.AD_FAIR_BID.value -> {
                this.appKey?.let { it1 ->
                    IKFairBidHelper.initialize(it1)
                }
            }

//            AdNetwork.PLAYGAP.value -> {
//                this.appKey?.let { it1 ->
//                    IKPlayGapHelper.initialize(it1)
//                }
//            }
        }
    }
}