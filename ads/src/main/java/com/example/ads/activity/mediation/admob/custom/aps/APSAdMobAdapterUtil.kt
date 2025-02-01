package com.example.ads.activity.mediation.admob.custom.aps

import android.os.Bundle
import android.os.Handler
import android.os.Looper
//import com.amazon.aps.ads.ApsAdRequest
//import com.amazon.aps.ads.model.ApsAdFormat
//import com.amazon.aps.shared.APSAnalytics
//import com.amazon.aps.shared.ApsMetrics
//import com.amazon.aps.shared.analytics.APSEventSeverity
//import com.amazon.aps.shared.analytics.APSEventType
//import com.amazon.device.ads.DtbCommonUtils

object APSAdMobAdapterUtil {
//    fun setupMetricsAndRemoteLogs() {
//        val adapterVersion: String =
//            "admob-" + APSAdMobAdapter.version
//        APSAnalytics.setAdapterVersion(adapterVersion)
//        ApsMetrics.adapterVersion = adapterVersion
//    }
//
//    fun createApsAdRequest(
//        slotId: String,
//        apsAdFormat: ApsAdFormat,
//        customEventExtras: Bundle
//    ): ApsAdRequest {
//        val apsAdRequest = ApsAdRequest(slotId, apsAdFormat)
//        try {
//            if (!customEventExtras.isEmpty) {
//                if (customEventExtras.containsKey("aps_privacy") && !DtbCommonUtils.isNullOrEmpty(
//                        customEventExtras.getString("aps_privacy")
//                    )
//                ) {
//                    apsAdRequest.putCustomTarget(
//                        "aps_privacy",
//                        customEventExtras.getString("aps_privacy")!!
//                    )
//                }
//                if (customEventExtras.containsKey("us_privacy") && !DtbCommonUtils.isNullOrEmpty(
//                        customEventExtras.getString("us_privacy")
//                    )
//                ) {
//                    apsAdRequest.putCustomTarget(
//                        "us_privacy",
//                        customEventExtras.getString("us_privacy")!!
//                    )
//                }
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Failed to get CCPA consent from customEventExtras",
//                e
//            )
//        }
//        return apsAdRequest
//    }
//
//    fun executeOnMainThread(proc: Runnable?) {
//        val handler = Handler(Looper.getMainLooper())
//        handler.post(proc!!)
//    }
}
