//package com.example.ads.activity.mediation.custom.fairbid
//
//import android.app.Activity
//import com.fyber.FairBid
//import com.fyber.fairbid.ads.FairBidListener
//import com.fyber.fairbid.ads.mediation.MediatedNetwork
//import com.fyber.fairbid.user.UserInfo
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.mediation.custom.IKCustomEventInitListener
//import com.example.ads.activity.mediation.custom.utils.IKInitializationStatus
//import java.util.concurrent.atomic.AtomicBoolean
//
//object IKFairBid {
//    private val isInitializing = AtomicBoolean()
//    private var status: IKInitializationStatus = IKInitializationStatus.NOT_INITIALIZED
//    fun initialize(
//        activity: Activity,
//        appId: String,
//        userIdentifier: String?,
//        listener: IKCustomEventInitListener
//    ) {
//        if (isInitializing.compareAndSet(false, true)) {
//            if (status != IKInitializationStatus.INITIALIZED_SUCCESS) {
//                status = IKInitializationStatus.INITIALIZING
//                userIdentifier?.let {
//                    UserInfo.setUserId(it)
//                }
//                FairBid.configureForAppId(appId).withFairBidListener(object : FairBidListener {
//                    override fun mediationFailedToStart(errorMessage: String, errorCode: Int) {
//                        status = IKInitializationStatus.INITIALIZED_FAILURE
//                        listener.onFail(IKAdError(errorCode, errorMessage))
//                        isInitializing.set(false)
//                    }
//
//                    override fun mediationStarted() {
//                        status = IKInitializationStatus.INITIALIZED_SUCCESS
//                        listener.onSuccess()
//                        isInitializing.set(false)
//                    }
//
//                    override fun onNetworkFailedToStart(
//                        network: MediatedNetwork,
//                        errorMessage: String
//                    ) {
//                    }
//
//                    override fun onNetworkStarted(network: MediatedNetwork) {
//                    }
//
//                }).start(activity)
//
//            } else {
//                listener.onSuccess()
//                isInitializing.set(false)
//            }
//        }
//    }
//}