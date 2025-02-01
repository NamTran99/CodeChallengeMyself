//package com.example.ads.activity.mediation.custom.fairbid
//
//import android.app.Activity
//import android.os.Bundle
//import com.applovin.sdk.AppLovinSdk
//import com.fyber.fairbid.ads.Interstitial
//import com.fyber.fairbid.ads.interstitial.InterstitialListener
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKCustomEventData
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.listener.sdk.IKSdkCustomEventAdListener
//import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.withTimeoutOrNull
//
//class IKFairBidInterstitial {
//    private var listUnit: MutableList<IKCustomEventData> = mutableListOf()
//    private var isAdLoading = false
//    suspend fun loadAd(
//        data: List<IKCustomEventData>,
//        listener: IKSdkCustomEventAdListener
//    ) {
//        val newUnits: List<IKCustomEventData> = withContext(Dispatchers.Default) {
//            runCatching {
//                data.sortedByDescending { it.p ?: 0 }
//            }.getOrDefault(emptyList())
//        }
//
//        val maxCurrent = newUnits.maxByOrNull { it.p ?: 0 }
//        val maxExisting = listUnit.maxByOrNull { it.p ?: 0 }
//
//        val existingUnits = listUnit.map { it.getUnitValue() }.toSet()
//        val newUniqueUnits = newUnits.filter { it.getUnitValue().isNotBlank() && it.getUnitValue() !in existingUnits }
//        listUnit.addAll(newUniqueUnits)
//
//        if (maxExisting != null && maxCurrent != null && (maxExisting.p ?: 0) >= (maxCurrent.p
//                ?: 0)
//        ) {
//            if (Interstitial.isAvailable(maxExisting.getUnitValue())) {
//                listener.onAdLoaded()
//                return
//            }
//        }
//        if (maxCurrent != null) {
//            if (Interstitial.isAvailable(maxCurrent.getUnitValue())) {
//                listener.onAdLoaded()
//                return
//            }
//        }
//
//        val loadList = newUnits.filter {
//            !(Interstitial.isAvailable(it.getUnitValue()) || it.isLoading)
//        }.toMutableList()
//        if (loadList.isEmpty()) {
//            if (isAdReady())
//                listener.onAdLoaded()
//            else
//                listener.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//            return
//        }
//
////        if (isAdLoading) {
////            if (isAdReady())
////                listener.onAdLoaded()
////            else
////                listener.onAdLoadFail(IKAdError(IKSdkErrorCode.CURRENT_AD_LOADING))
////            return
////        }
//
//        isAdLoading = true
//        val interstitialEvents = MutableSharedFlow<Boolean>()
//        Interstitial.setInterstitialListener(object : InterstitialListener {
//            override fun onShow(
//                placementId: String,
//                impressionData: com.fyber.fairbid.ads.ImpressionData
//            ) {
//                val creativeId: String? = impressionData.creativeId
//                if (AppLovinSdk.VERSION_CODE >= 9150000 && !creativeId.isNullOrBlank()) {
//                    val extraInfo = Bundle(1)
//                    extraInfo.putString("creative_id", creativeId)
//                    listener.onAdShowed(extraInfo)
//                } else {
//                    listener.onAdShowed()
//                }
//                listener.onAdImpression()
//            }
//
//            override fun onClick(placementId: String) {
//                listener.onAdClicked()
//            }
//
//            override fun onHide(placementId: String) {
//                listener.onAdDismissed()
//            }
//
//            override fun onShowFailure(
//                placementId: String,
//                impressionData: com.fyber.fairbid.ads.ImpressionData
//            ) {
//                listener.onAdShowFailed(IKAdError(IKSdkErrorCode.SHOWING_FAIL))
//            }
//
//            override fun onAvailable(placementId: String) {
//                interstitialEvents.tryEmit(true)
//            }
//
//            override fun onUnavailable(placementId: String) {
//                interstitialEvents.tryEmit(false)
//            }
//
//            override fun onRequestStart(placementId: String, requestId: String) {
//                // Called when an interstitial from placement 'placementId' is going to be requested
//                // 'requestId' identifies the request across the whole request/show flow
//            }
//        })
//        try {
//            for (dto in loadList) {
//                Interstitial.request(dto.getUnitValue())
//                dto.isLoading = true
//                val result = withTimeoutOrNull(dto.getTimeValue()) {
//                    interstitialEvents.first { it }
//                }
//                dto.isLoading = false
//                if (result != null) {
//                    break
//                }
//            }
//        } finally {
//            isAdLoading = false
//            val maxCurrentP = maxCurrent?.p ?: 0
//            val adLoaded = withContext(Dispatchers.Default) {
//                listUnit.find {
//                    (it.p ?: 0) >= maxCurrentP && Interstitial.isAvailable(it.getUnitValue())
//                } != null
//            }
//            if (adLoaded)
//                listener.onAdLoaded()
//            else {
//                listener.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
//            }
//        }
//        withContext(Dispatchers.Main) {
//            delay(40000)
//            isAdLoading = false
//        }
//    }
//
//    suspend fun isAdReady(): Boolean {
//        return withContext(Dispatchers.Default) {
//            listUnit = listUnit.sortedByDescending { it.p ?: 0 }.toMutableList()
//
//            listUnit.find {
//                Interstitial.isAvailable(it.getUnitValue())
//            } != null
//        }
//    }
//
//    suspend fun isMaxAdReady(): Boolean {
//        withContext(Dispatchers.Default) {
//            listUnit = listUnit.sortedByDescending { it.p ?: 0 }.toMutableList()
//        }
//        return listUnit.find {
//            Interstitial.isAvailable(it.getUnitValue())
//        } != null
//    }
//
//    suspend fun showAd(activity: Activity, listener: IKSdkShowAdListener) {
//        withContext(Dispatchers.Default) {
//            listUnit = listUnit.sortedByDescending { it.p ?: 0 }.toMutableList()
//
//            val ad = listUnit.find {
//                Interstitial.isAvailable(it.getUnitValue())
//            }
//            if (ad != null) {
//                listener.onAdShowed(0)
//                launch(Dispatchers.Main) {
//                    Interstitial.show(ad.getUnitValue(), activity)
//                }
//            } else {
//                listener.onAdShowFail(IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW))
//            }
//        }
//    }
//
//    fun destroy() {
//        listUnit.clear()
//    }
//
//}