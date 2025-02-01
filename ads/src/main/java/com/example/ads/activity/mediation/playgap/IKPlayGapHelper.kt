//package com.example.ads.activity.mediation.playgap
//
//import com.example.ads.activity.core.IKSdkApplicationProvider
//import com.example.ads.activity.utils.IKLogs
//import io.playgap.sdk.InitError
//import io.playgap.sdk.InitializationListener
//import io.playgap.sdk.PlaygapAds
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.util.concurrent.atomic.AtomicBoolean
//
//object IKPlayGapHelper {
//    var initStatus = false
//        private set
//    private val isInitializing = AtomicBoolean()
//    private const val TAG = "IKPlayGapHelper"
//    fun initialize(appKey: String) {
//        IKLogs.dNone(TAG) { "start init" }
//        if (initStatus)
//            return
//        if (!isInitializing.compareAndSet(false, true))
//            return
//        if (!hasLib()) {
//            isInitializing.set(false)
//            return
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            kotlin.runCatching {
//                IKLogs.dNone(TAG) { "run init" }
//                IKSdkApplicationProvider.getContext()?.let {
//                    PlaygapAds.initialize(
//                        context = it,
//                        apiKey = appKey,
//                        listener = object : InitializationListener {
//                            override fun onInitializationError(error: InitError) {
//                                isInitializing.set(false)
//                                IKLogs.d(TAG) { "start onInitializationError,$error" }
//                            }
//
//                            override fun onInitialized() {
//                                isInitializing.set(false)
//                                initStatus = true
//                                IKLogs.dNone(TAG) { "start onInitialized" }
//                            }
//                        }
//                    )
//                }
//            }.onFailure {
//                isInitializing.set(false)
//                IKLogs.dNone(TAG) { "init fail:${it.message}" }
//            }
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(30000)
//                isInitializing.set(false)
//            }
//        }
//    }
//
//    fun hasLib(): Boolean {
//        return kotlin.runCatching {
//            Class.forName(PlaygapAds::class.java.name)
//            IKLogs.d(TAG) { "has Init" }
//            true
//        }.onFailure {
//            IKLogs.d(TAG) { "not Init" }
//        }.getOrDefault(false)
//    }
//
//    fun isInitialized(): Boolean {
//        if (initStatus)
//            return true
//        var isInit = false
//        kotlin.runCatching {
//            Class.forName(PlaygapAds::class.java.name)
//            IKLogs.d(TAG) { "has Init" }
//            isInit = true
//        }.onFailure {
//            IKLogs.d(TAG) { "not Init" }
//            isInit = false
//        }
//
//        return isInit
//    }
//
//
//}