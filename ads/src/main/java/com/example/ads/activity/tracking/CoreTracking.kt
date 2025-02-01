//package com.example.ads.activity.tracking
//
//import android.content.Context
//import android.os.Bundle
//import android.util.Log
//import com.adjust.sdk.Adjust
//import com.adjust.sdk.AdjustEvent
//import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.crashlytics.ktx.crashlytics
//import com.google.firebase.ktx.Firebase
//import com.example.ads.activity.core.IKSdkApplicationProvider
//import com.example.ads.activity.core.firebase.IKSdkFirebaseModule
//import com.example.ads.activity.utils.IKLogs
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.subStringEvent
//import com.example.ads.activity.utils.IKSdkUtilsCore
//import com.example.ads.activity.utils.IKTrackingConst.EventName
//import com.example.ads.activity.utils.IKTrackingConst.ParamName
//import com.example.ads.activity.utils.IKTrackingConst.mapNewAdFormat
//import com.example.ads.activity.utils.IkmSdkCoreFunc
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import java.util.Locale
//import java.util.concurrent.TimeUnit
//
//object CoreTracking {
//
//    private fun showLogTrackingSdk(tag: String, level: Int = Log.DEBUG, message: () -> String) {
//        IKLogs.trackingLogSdk("TrackingLog", level) {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//    private fun showLogTracking(tag: String, level: Int = Log.DEBUG, message: () -> String) {
//        IKLogs.trackingLog("TrackingLog", level) {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//    private var uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//
//    private fun sendLogEvent(eventName: String, params: Bundle) {
//        kotlin.runCatching {
//            IKSdkApplicationProvider.getContext()?.let {
//                IKSdkFirebaseModule.provideIKFirebaseAnalytics().logEvent(it, eventName, params)
//            }
//            showLogTrackingSdk("logEvent") { "$eventName: $params" }
//        }.onFailure {
//            showLogTrackingSdk("logEvent Error", Log.ERROR) { "$eventName: $params" }
//        }
//    }
//
//    private fun sendLogEventPub(eventName: String, params: Bundle) {
//        kotlin.runCatching {
//            IKSdkApplicationProvider.getContext()?.let {
//                IKSdkFirebaseModule.provideIKFirebaseAnalytics().logEvent(it, eventName, params)
//            }
//            showLogTracking("logEvent") { "$eventName: $params" }
//        }.onFailure {
//            showLogTracking("logEvent Error", Log.ERROR) { "$eventName: $params" }
//        }
//    }
//
//    fun logUserId(value: String) {
//        try {
//            val bundle = Bundle()
//            bundle.putString("id", value)
//            sendLogEvent("user_id", bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun logIgnoreUserBilling(value: String) {
//        try {
//            val bundle = Bundle()
//            bundle.putString("user_detail", value)
//            sendLogEvent("user_billing", bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun logEventRetention(context: Context?) {
//        try {
//            val bundle = Bundle()
//            var mDayUseApp = 0L
//            try {
//                val time = IKSdkUtilsCore.getAppPackageInfo(context)?.firstInstallTime
//                    ?: System.currentTimeMillis()
//                mDayUseApp = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - time)
//            } catch (_: Exception) {
//
//            }
//            var txt = "d$mDayUseApp"
//            if (mDayUseApp in 8..13)
//                txt = "d$8+"
//            else if (mDayUseApp in 14..29)
//                txt = "d$14+"
//            bundle.putString("day", txt)
//            sendLogEvent("retention", bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun trackingSdkAd(
//        adFormat: String,
//        adStatus: String,
//        screen: String,
//        actionWithAds: String,
//        vararg multiValue: Pair<String, String>
//    ) {
//        try {
//            val bundle = Bundle()
//            val statusInternet: Boolean = IkmSdkCoreFunc.AppF?.isInternetAvailable == true
//            bundle.putString(
//                ParamName.STATUS_INTERNET,
//                if (statusInternet) ParamName.YES else ParamName.NO
//            )
//            bundle.putString(ParamName.SUB_AD_FORMAT, adFormat)
//            bundle.putString(ParamName.AD_FORMAT, mapNewAdFormat(adFormat))
//            bundle.putString(ParamName.AD_POSITION, screen)
//            bundle.putString(ParamName.AD_STATUS, adStatus)
//            bundle.putString(ParamName.AD_ACTION, actionWithAds)
//            multiValue.forEach {
//                bundle.putString(it.first, it.second)
//            }
//            sendLogEvent(EventName.AD_TRACK, bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun trackingSdkBackupAd(
//        adFormat: String,
//        adStatus: String,
//        screen: String,
//        actionWithAds: String,
//        vararg multiValue: Pair<String, String>
//    ) {
//        try {
//            val bundle = Bundle()
//            val statusInternet: Boolean = IkmSdkCoreFunc.AppF?.isInternetAvailable == true
//            bundle.putString(
//                ParamName.STATUS_INTERNET,
//                if (statusInternet) ParamName.YES else ParamName.NO
//            )
//            bundle.putString(ParamName.SUB_AD_FORMAT, adFormat)
//            bundle.putString(ParamName.AD_FORMAT, mapNewAdFormat(adFormat))
//            bundle.putString(ParamName.AD_POSITION, screen)
//            bundle.putString(ParamName.AD_STATUS, adStatus)
//            bundle.putString(ParamName.AD_ACTION, actionWithAds)
//            multiValue.forEach {
//                bundle.putString(it.first, it.second)
//            }
//            sendLogEvent(EventName.AD_TRACK_NATIVE_LATEST, bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun measureAdRevenue(
//        platform: String,
//        adUnitName: String,
//        adFormat: String,
//        adSource: String,
//        revenue: Double
//    ) {
//        try {
//            val analytics =
//                IKSdkApplicationProvider.getContext()?.let { FirebaseAnalytics.getInstance(it) }
//            val bundle = Bundle()
//            bundle.apply {
//                putString(FirebaseAnalytics.Param.AD_PLATFORM, platform)
//                putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnitName)
//                putString(FirebaseAnalytics.Param.AD_FORMAT, adFormat)
//                putString(FirebaseAnalytics.Param.AD_SOURCE, adSource)
//                putDouble(FirebaseAnalytics.Param.VALUE, revenue)
//                putString(FirebaseAnalytics.Param.CURRENCY, IKSdkDefConst.CURRENCY_CODE_USD)
//            }
//            analytics?.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle)
//        } catch (_: Exception) {
//        }
//    }
//
//    fun trackingCustomPaidAd(
//        eventName: String,
//        valueMicros: Double,
//        currency: String?,
//        adUnitId: String?,
//        network: String?,
//        adPlatform: String,
//        adFormat: String,
//        enableTrackingThreshold: Boolean,
//        adId: String,
//        screen: String
//    ) {
//        try {
//            val params = Bundle()
//            params.putString(ParamName.AD_PLATFORM, adPlatform.uppercase(Locale.getDefault()))
//            params.putString(ParamName.CURRENCY, currency ?: IKSdkDefConst.CURRENCY_CODE_USD)
//            params.putDouble(ParamName.VALUE, valueMicros)
//            params.putString(ParamName.AD_UNIT, adUnitId)
//            params.putString(ParamName.AD_NETWORK, network)
//            params.putString(ParamName.SUB_AD_FORMAT, adFormat)
//            params.putString(ParamName.AD_FORMAT, mapNewAdFormat(adFormat))
//            params.putString(ParamName.AD_ID, adId)
//            params.putString(ParamName.AD_POSITION, screen)
//            sendLogEvent(eventName, params)
//        } catch (_: Exception) {
//        }
//    }
//
//
//    fun trackingCustomAdjust(token: String) {
//        if (token.isBlank())
//            return
//        val adjustEvent = AdjustEvent(token)
//        Adjust.trackEvent(adjustEvent)
//        showLogTrackingSdk("trackingCustomAdjust") { "token=$token" }
//    }
//
//    fun customizeTracking(
//        eventName: String,
//        isPub: Boolean = false,
//        vararg param: Pair<String, String?>
//    ) {
//        try {
//            val bundle = Bundle()
//            val statusInternet: Boolean = IKSdkUtilsCore.isConnectionAvailable()
//            bundle.putString(
//                ParamName.STATUS_INTERNET,
//                if (statusInternet) ParamName.YES else ParamName.NO
//            )
//            param.forEach {
//                val mes: MutableList<Pair<String, String>> = mutableListOf()
//                (it.second ?: IKSdkDefConst.UNKNOWN).subStringEvent()
//                    .forEachIndexed { index, s ->
//                        if (index == 0)
//                            mes.add(Pair(it.first.trim(), s))
//                        else
//                            mes.add(Pair(it.first.trim() + "$index", s))
//                    }
//                if (mes.size <= 1) {
//                    bundle.putString(
//                        it.first.trim(),
//                        (it.second ?: IKSdkDefConst.UNKNOWN)
//                    )
//                } else {
//                    mes.forEach { m ->
//                        bundle.putString(
//                            m.first.trim(),
//                            m.second
//                        )
//                    }
//                }
//            }
//            if (isPub)
//                sendLogEventPub(eventName, bundle)
//            else
//                sendLogEvent(eventName, bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun customizeTracking(
//        eventName: String,
//        isPub: Boolean = false,
//        bundle: Bundle
//    ) {
//        try {
//            val statusInternet: Boolean = IkmSdkCoreFunc.AppF?.isInternetAvailable == true
//            bundle.putString(
//                ParamName.STATUS_INTERNET,
//                if (statusInternet) ParamName.YES else ParamName.NO
//            )
//            if (isPub)
//                sendLogEventPub(eventName, bundle)
//            else
//                sendLogEvent(eventName, bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun customizeTrackingAdjustPartner(
//        token: String,
//        param: HashMap<String, String>
//    ) {
//        if (token.isBlank())
//            return
//        kotlin.runCatching {
//            val adjustEvent = AdjustEvent(token)
//            param.forEach {
//                adjustEvent.addPartnerParameter(it.key, it.value)
//            }
//            Adjust.trackEvent(adjustEvent)
//            showLogTrackingSdk("customizeTrackingAdjustPartner") { "token=$token,param=$param" }
//        }
//    }
//
//    fun trackingAdjustPartnerWithRevenue(
//        token: String,
//        revenue: Double,
//        currencyCode: String,
//        param: HashMap<String, String>
//    ) {
//        if (token.isBlank())
//            return
//        kotlin.runCatching {
//            val adjustEvent = AdjustEvent(token)
//            adjustEvent.setRevenue(revenue, currencyCode)
//            param.forEach {
//                adjustEvent.addPartnerParameter(it.key, it.value)
//            }
//            Adjust.trackEvent(adjustEvent)
//            showLogTrackingSdk("trackingAdjustPartnerWithRevenue") { "token=$token,param=$param" }
//        }
//    }
//
//    fun customizeTrackingAdjustCallback(
//        token: String,
//        param: HashMap<String, String>
//    ) {
//        if (token.isBlank())
//            return
//        kotlin.runCatching {
//            val adjustEvent = AdjustEvent(token)
//            param.forEach {
//                adjustEvent.addCallbackParameter(it.key, it.value)
//            }
//            Adjust.trackEvent(adjustEvent)
//            showLogTrackingSdk("customizeTrackingAdjustCallback") { "token=$token,param=$param" }
//        }
//    }
//
//    fun customizeTrackingAdjustPartner(
//        token: String,
//        revenue: Double,
//        currencyCode: String,
//        param: HashMap<String, String>
//    ) {
//        if (token.isBlank())
//            return
//        kotlin.runCatching {
//            val adjustEvent = AdjustEvent(token)
//            adjustEvent.setRevenue(revenue / IKSdkDefConst.DATA_RV_DIV_USD, currencyCode)
//            param.forEach {
//                adjustEvent.addPartnerParameter(it.key, it.value)
//            }
//            Adjust.trackEvent(adjustEvent)
//            showLogTrackingSdk("customizeTrackingAdjustPartner") { "token=$token,param=$param" }
//        }
//    }
//
//    fun trackingAdjustInAppPurchase(
//        token: String,
//        revenue: Double,
//        currencyCode: String,
//        param: HashMap<String, String>
//    ) {
//        if (token.isBlank())
//            return
//        kotlin.runCatching {
//            val adjustEvent = AdjustEvent(token)
//            adjustEvent.setRevenue(revenue / IKSdkDefConst.DATA_RV_DIV_USD, currencyCode)
//            param.forEach {
//                adjustEvent.addPartnerParameter(it.key, it.value)
//            }
//            adjustEvent.productId = param[ParamName.PRODUCT_ID]
//            adjustEvent.purchaseToken = param[ParamName.PURCHASE_TOKEN]
//            adjustEvent.orderId = param[ParamName.ORDER_ID]
//            Adjust.trackEvent(adjustEvent)
//            Adjust.verifyAndTrackPlayStorePurchase(adjustEvent) {
//            }
//            showLogTrackingSdk("trackingAdjustInAppPurchase") { "token=$token,param=$param" }
//        }
//    }
//
//    fun customizeTrackingAdjustCallback(
//        token: String,
//        revenue: Double,
//        currencyCode: String,
//        param: HashMap<String, String>
//    ) {
//        if (token.isBlank())
//            return
//        kotlin.runCatching {
//            val adjustEvent = AdjustEvent(token)
//            adjustEvent.setRevenue(revenue / IKSdkDefConst.DATA_RV_DIV_USD, currencyCode)
//            param.forEach {
//                adjustEvent.addCallbackParameter(it.key, it.value)
//            }
//            Adjust.trackEvent(adjustEvent)
//            showLogTrackingSdk("customizeTrackingAdjustCallback") { "token=$token,param=$param" }
//        }
//    }
//
//    fun setCrashlyticsCustomKeys(
//        vararg param: Pair<String, Any>
//    ) {
//        runCatching {
//            val crashlytics = Firebase.crashlytics
//            param.forEach {
//                when (it.second) {
//                    is String -> crashlytics.setCustomKey(it.first, it.second as String)
//                    is Int -> crashlytics.setCustomKey(it.first, it.second as Int)
//                    is Double -> crashlytics.setCustomKey(it.first, it.second as Double)
//                    is Boolean -> crashlytics.setCustomKey(it.first, it.second as Boolean)
//                    is Long -> crashlytics.setCustomKey(it.first, it.second as Long)
//                    is Float -> crashlytics.setCustomKey(it.first, it.second as Float)
//                }
//            }
//        }
//    }
//
//    fun setUserRetention(context: Context?) {
//        try {
//            var mDayUseApp = 0L
//            try {
//                val time = IKSdkUtilsCore.getAppPackageInfo(context)?.firstInstallTime
//                    ?: System.currentTimeMillis()
//                mDayUseApp = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - time)
//            } catch (_: Exception) {
//            }
//            context?.let {
//                FirebaseAnalytics.getInstance(it)
//                    .setUserProperty("ik_retention", "$mDayUseApp")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//}
