package com.example.ads.activity.mediation.admob.custom.aps

import android.os.Bundle
//import com.amazon.aps.ads.ApsAd
//import com.amazon.aps.ads.ApsAdController
//import com.amazon.aps.ads.ApsAdError
//import com.amazon.aps.ads.ApsAdFormatUtils
//import com.amazon.aps.ads.listeners.ApsAdRequestListener
//import com.amazon.aps.ads.model.ApsAdFormat
//import com.amazon.aps.shared.ApsMetrics.Companion.adapterEvent
//import com.amazon.aps.shared.metrics.ApsMetricsPerfEventModelBuilder
//import com.amazon.aps.shared.metrics.model.ApsMetricsResult
//import com.amazon.device.ads.AdRegistration
//import com.amazon.device.ads.AdType
//import com.amazon.device.ads.DTBAdUtil
//import com.amazon.device.ads.DTBCacheData
//import com.amazon.device.ads.DtbCommonUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.mediation.MediationAdLoadCallback

object APSAdMobUtil {
//
//    fun loadBannerAd(
//        apsAdController: ApsAdController,
//        listener: MediationAdLoadCallback<*, *>,
//        customEventExtras: Bundle,
//        serverParameter: String?,
//        autoRefreshRequestIds: MutableSet<String>,
//        metricsBuilder: ApsMetricsPerfEventModelBuilder,
//        correlationId: String?
//    ) {
//        customEventExtras.getString("amazon_custom_event_slot_group")
//        val slotUUID = customEventExtras.getString("amazon_custom_event_slot_uuid")
//        val width = customEventExtras.getInt("amazon_custom_event_width")
//        val height = customEventExtras.getInt("amazon_custom_event_height")
//        val requestId = customEventExtras.getString("amazon_custom_event_request_id")
//        if (DtbCommonUtils.isNullOrEmpty(customEventExtras.getString("amazon_custom_event_request_id"))) {
//            listener.onFailure(
//                AdError(
//                    3,
//                    "Fail to load custom banner ad in loadBannerAd because no request id found",
//                    "com.amazon.device.ads"
//                )
//            )
//        } else if (!slotUUID.isNullOrBlank() && width > 0 && height > 0) {
//            val apsAdRequest = APSAdMobAdapterUtil.createApsAdRequest(
//                slotUUID, ApsAdFormatUtils.getAdFormat(
//                    AdType.DISPLAY, height, width
//                ) ?: ApsAdFormat.BANNER, customEventExtras
//            )
//            apsAdRequest.correlationId = correlationId
//            if (autoRefreshRequestIds.contains(requestId)) {
//                apsAdRequest.setRefreshFlag(true)
//            } else {
//                if (requestId != null) {
//                    autoRefreshRequestIds.add(requestId)
//                }
//            }
//            val dtbCacheData = DTBCacheData(requestId, apsAdRequest)
//            AdRegistration.addAdMobCache(requestId, dtbCacheData)
//            apsAdRequest.loadAd(object : ApsAdRequestListener {
//                override fun onFailure(adError: ApsAdError) {
//                    dtbCacheData.isBidRequestFailed = true
//                    listener.onFailure(
//                        AdError(
//                            3,
//                            "Fail to load custom banner ad in requestBannerAd in APSAdMobCustomBannerEvent class",
//                            "com.amazon.device.ads"
//                        )
//                    )
//                }
//
//                override fun onSuccess(apsAd: ApsAd) {
//                    dtbCacheData.addResponse(apsAd)
//                    metricsBuilder.withBidId(apsAd.bidId)
//                    renderAPSBannerAds(
//                        apsAd,
//                        apsAdController,
//                        listener,
//                        serverParameter,
//                        requestId
//                    )
//                }
//            })
//        } else {
//            listener.onFailure(
//                AdError(
//                    3,
//                    "Fail to load custom banner ad in loadBannerAd",
//                    "com.amazon.device.ads"
//                )
//            )
//        }
//    }
//
//    fun renderAPSBannerAds(
//        apsAd: ApsAd,
//        apsAdController: ApsAdController,
//        listener: MediationAdLoadCallback<*, *>,
//        serverParameter: String?,
//        requestId: String?,
//    ) {
//        if (DTBAdUtil.validateSinglePriceAdMobCustomEvent(serverParameter, apsAd.renderingBundle)) {
//            apsAdController.fetchAd(apsAd)
//            AdRegistration.removeAdMobCache(requestId)
//            return
//        }
//        listener.onFailure(
//            AdError(
//                3,
//                "Fail to load custom banner ad in renderAPSBannerAds",
//                "com.amazon.device.ads"
//            )
//        )
//    }
//
//    fun loadInterstitialAd(
//        apsAdController: ApsAdController,
//        listener: MediationAdLoadCallback<*, *>,
//        customEventExtras: Bundle,
//        serverParameter: String?,
//        metricsBuilder: ApsMetricsPerfEventModelBuilder,
//        correlationId: String?
//    ) {
//        val slotUUID = customEventExtras.getString("amazon_custom_event_slot_uuid")
//        val requestId = customEventExtras.getString("amazon_custom_event_request_id")
//        if (DtbCommonUtils.isNullOrEmpty(customEventExtras.getString("amazon_custom_event_request_id"))) {
//            listener.onFailure(
//                AdError(
//                    3,
//                    "Fail to load custom banner ad in loadInterstitialAd because previous bid requests failure",
//                    "com.amazon.device.ads"
//                )
//            )
//        } else if (!slotUUID.isNullOrBlank()) {
//            val apsAdRequest = APSAdMobAdapterUtil.createApsAdRequest(
//                slotUUID,
//                ApsAdFormat.INTERSTITIAL,
//                customEventExtras
//            )
//            apsAdRequest.correlationId = correlationId
//            val dtbCacheData = DTBCacheData(requestId, apsAdRequest)
//            AdRegistration.addAdMobCache(requestId, dtbCacheData)
//            apsAdRequest.loadAd(object : ApsAdRequestListener {
//                override fun onFailure(adError: ApsAdError) {
//                    dtbCacheData.isBidRequestFailed = true
//                    listener.onFailure(
//                        AdError(
//                            3,
//                            "Fail to load custom interstitial ad in loadInterstitialAd",
//                            "com.amazon.device.ads"
//                        )
//                    )
//                }
//
//                override fun onSuccess(apsAd: ApsAd) {
//                    dtbCacheData.addResponse(apsAd)
//                    metricsBuilder.withBidId(apsAd.bidId)
//                    renderAPSInterstitialAds(
//                        apsAd,
//                        apsAdController,
//                        listener,
//                        serverParameter,
//                        requestId
//                    )
//                }
//            })
//        } else {
//            listener.onFailure(
//                AdError(
//                    3,
//                    "Fail to load custom interstitial ad in loadInterstitialAd",
//                    "com.amazon.device.ads"
//                )
//            )
//        }
//    }
//
//    fun renderAPSInterstitialAds(
//        apsAd: ApsAd,
//        apsAdController: ApsAdController,
//        listener: MediationAdLoadCallback<*, *>,
//        serverParameter: String?,
//        requestId: String?
//    ) {
//        if (DTBAdUtil.validateSinglePriceAdMobCustomEvent(serverParameter, apsAd.renderingBundle)) {
//            apsAdController.fetchAd(apsAd)
//            AdRegistration.removeAdMobCache(requestId)
//            return
//        }
//        listener.onFailure(
//            AdError(
//                3,
//                "Fail to load custom interstitial ad in renderAPSInterstitialAds method",
//                "com.amazon.device.ads"
//            )
//        )
//    }
//
//    fun captureAdapterEndEvent(
//        result: ApsMetricsResult?,
//        metricsBuilder: ApsMetricsPerfEventModelBuilder,
//        correlationId: String
//    ) {
//        if (result != null) {
//            metricsBuilder.withAdapterEndTime(result, System.currentTimeMillis())
//            metricsBuilder.withCorrelationId(correlationId)
//            adapterEvent(null as String?, metricsBuilder)
//        }
//    }
}
