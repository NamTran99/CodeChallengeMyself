//package com.example.ads.activity.tracking
//
//import android.content.Context
//import android.os.Bundle
//import com.adjust.sdk.Adjust
//import com.adjust.sdk.AdjustEvent
//import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import com.example.ads.activity.data.dto.pub.IKAdjustAttribution
//import com.example.ads.activity.data.dto.pub.IKTrackingConst
//import com.example.ads.activity.listener.pub.IKTrackingHelperInterface
//import com.example.ads.activity.utils.IKLogs
//
//object IKTrackingHelper : IKTrackingHelperInterface {
//    override fun sendTracking(
//        eventName: String,
//        vararg param: Pair<String, String?>
//    ) {
//        CoreTracking.customizeTracking(eventName, true, *param)
//    }
//
//    override fun sendTracking(
//        eventName: String,
//        bundle: Bundle
//    ) {
//        CoreTracking.customizeTracking(eventName, true, bundle)
//    }
//
//    override fun logEvent(
//        eventName: String,
//        vararg param: Pair<String, String?>
//    ) {
//        CoreTracking.customizeTracking(eventName, true, *param)
//    }
//
//    override fun logEvent(
//        eventName: String,
//        bundle: Bundle
//    ) {
//        CoreTracking.customizeTracking(eventName, true, bundle)
//    }
//
//    override fun setUserProperty(
//        context: Context?,
//        eventName: String,
//        value: String,
//    ) {
//        if (context != null) {
//            FirebaseAnalytics.getInstance(context)
//                .setUserProperty(eventName, value)
//        }
//    }
//
//    override fun logCrash(message: String) {
//        FirebaseCrashlytics.getInstance().log(message)
//    }
//
//    override fun trackingAdjustCamp(token: String, adjustAttribution: IKAdjustAttribution) {
//        try {
//            val adjustEvent = AdjustEvent(token)
//            adjustEvent.addPartnerParameter("trackerToken", adjustAttribution.trackerToken)
//            adjustEvent.addPartnerParameter("trackerName", adjustAttribution.trackerName)
//            adjustEvent.addPartnerParameter("network", adjustAttribution.network)
//            adjustEvent.addPartnerParameter("campaign", adjustAttribution.campaign)
//            adjustEvent.addPartnerParameter("adgroup", adjustAttribution.adgroup)
//            adjustEvent.addPartnerParameter("creative", adjustAttribution.creative)
//            adjustEvent.addPartnerParameter("clickLabel", adjustAttribution.clickLabel)
//            adjustEvent.addPartnerParameter("adid", adjustAttribution.adid)
//            adjustEvent.addPartnerParameter("costType", adjustAttribution.costType)
//            adjustEvent.addPartnerParameter("costAmount", adjustAttribution.costAmount?.toString())
//            adjustEvent.addPartnerParameter("costCurrency", adjustAttribution.costCurrency)
//            adjustEvent.addPartnerParameter(
//                "fbInstallReferrer",
//                adjustAttribution.fbInstallReferrer
//            )
//            Adjust.trackEvent(adjustEvent)
//            kotlin.runCatching {
//                IKLogs.trackingLog("TrackingLog") {
//                    "trackingAdjustCamp:$adjustEvent"
//                }
//            }
//        } catch (_: Exception) {
//        }
//    }
//
//    override fun trackingAdjustEvent(eventName: String, vararg param: Pair<String, String?>) {
//        try {
//            val adjustEvent = AdjustEvent(eventName)
//            param.forEach {
//                adjustEvent.addCallbackParameter(it.first.trim(), it.second?.trim())
//            }
//            Adjust.trackEvent(adjustEvent)
//            kotlin.runCatching {
//                IKLogs.trackingLog("TrackingLog") {
//                    "trackingAdjustEvent:$adjustEvent"
//                }
//            }
//        } catch (_: Exception) {
//        }
//    }
//
//    override fun setAdjustPushToken(context: Context, token: String) {
//        try {
//            Adjust.setPushToken(token, context)
//            kotlin.runCatching {
//                IKLogs.trackingLog("TrackingLog") {
//                    "setAdjustPushToken:$token"
//                }
//            }
//        } catch (_: Exception) {
//        }
//    }
//
//    override fun sendTrackingPermission(
//        actionName: IKTrackingConst.PermissionActionName,
//        from: String?,
//        permissionName: String?,
//        status: IKTrackingConst.PermissionStatus?
//    ) {
//        val bundle = Bundle()
//        bundle.putString("action_name", actionName.value)
//        if (!from.isNullOrBlank())
//            bundle.putString("from", from)
//        if (!permissionName.isNullOrBlank())
//            bundle.putString("permission_name", permissionName)
//        if (status != null)
//            bundle.putString("status", status.value)
//        CoreTracking.customizeTracking("permission", true, bundle)
//    }
//
//    override fun sendTrackingPermission(
//        actionName: IKTrackingConst.PermissionActionName,
//        from: String?,
//        permissionName: String?
//    ) {
//        val bundle = Bundle()
//        bundle.putString("action_name", actionName.value)
//        if (!from.isNullOrBlank())
//            bundle.putString("from", from)
//        if (!permissionName.isNullOrBlank())
//            bundle.putString("permission_name", permissionName)
//        CoreTracking.customizeTracking("permission", true, bundle)
//    }
//
//    override fun sendTrackingNotification(
//        actionName: IKTrackingConst.NotificationActionName,
//        notifyType: String?,
//        notifyContent: String?
//    ) {
//        val bundle = Bundle()
//        bundle.putString("action_name", actionName.value)
//        if (!notifyType.isNullOrBlank())
//            bundle.putString("noti_type", notifyType)
//        if (!notifyContent.isNullOrBlank())
//            bundle.putString("noti_content", notifyContent)
//        CoreTracking.customizeTracking("notification", true, bundle)
//    }
//
//    override fun sendTrackingFeedback(
//        actionName: IKTrackingConst.FeedbackActionName,
//        value: String?
//    ) {
//        val bundle = Bundle()
//        bundle.putString("action_name", actionName.value)
//        if (!value.isNullOrBlank())
//            bundle.putString("value", value)
//        CoreTracking.customizeTracking("feedback", true, bundle)
//    }
//
//    override fun setCrashlyticsCustomKeys(
//        vararg param: Pair<String, Any>
//    ) {
//        CoreTracking.setCrashlyticsCustomKeys(*param)
//    }
//
//}
