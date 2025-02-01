package com.example.ads.activity.listener.pub

import android.content.Context
import android.os.Bundle
import com.example.ads.activity.data.dto.pub.IKAdjustAttribution
import com.example.ads.activity.data.dto.pub.IKTrackingConst

interface IKTrackingHelperInterface {
    fun sendTracking(eventName: String, vararg param: Pair<String, String?>)
    fun sendTracking(eventName: String, bundle: Bundle)
    fun logEvent(eventName: String, vararg param: Pair<String, String?>)
    fun logEvent(eventName: String, bundle: Bundle)
    fun setUserProperty(context: Context?, eventName: String, value: String)
    fun logCrash(message: String)
    fun trackingAdjustCamp(token: String, adjustAttribution: IKAdjustAttribution)
    fun trackingAdjustEvent(eventName: String, vararg param: Pair<String, String?>)
    fun setAdjustPushToken(context: Context, token: String)
    /**
     * Sends tracking information related to permission events.
     *
     * @param actionName The action name for the permission event.
     * @param from The source or context of the permission request (e.g., "popup").
     * @param permissionName The name of the permission being requested.
     * @param status The status of the permission (e.g., "yes", "no").
     *
     * **Event:** permission
     * **Params.key:** "action_name", "from", "permission_name"
     * **Params.Value:** "popup", "[...]", "[...]"
     * **Data type:** string
     * **Description:**
     * - Default action
     * - Popup source (e.g., which screen triggered the popup)
     * - Permission name
     * **Trigger:** When the permission request popup is displayed.
     */
    fun sendTrackingPermission(
        actionName: IKTrackingConst.PermissionActionName,
        from: String?,
        permissionName: String?,
        status: IKTrackingConst.PermissionStatus?
    )

    /**
     * Sends tracking information related to permission events.
     *
     * @param actionName The action name for the permission event.
     * @param from The source or context of the permission request (e.g., "popup").
     * @param permissionName The name of the permission being requested.
     *
     * **Event:** permission
     * **Params.key:** "action_name", "permission_name"
     * **Params.Value:** "grant_permission", "[...]"
     * **Data type:** string
     * **Description:**
     * - Default action
     * - Permission name
     * **Trigger:** When the user grants the permission.
     */
    fun sendTrackingPermission(
        actionName: IKTrackingConst.PermissionActionName,
        from: String?,
        permissionName: String?
    )

    /**
     * Sends tracking information related to notification events.
     *
     * @param actionName The action name for the notification event.
     * @param notifyType The type of notification.
     * @param notifyContent The content of the notification.
     *
     * **Event:** notification
     * **Params.key:** "action_name", "noti_type", "noti_content"
     * **Params.Value:** "show", "[...]", "[...]"
     * **Data type:** string
     * **Description:**
     * - Default action
     * - Notification type
     * - Notification content
     * **Trigger:** When the notification is displayed.
     */
    fun sendTrackingNotification(
        actionName: IKTrackingConst.NotificationActionName,
        notifyType: String?,
        notifyContent: String?
    )

    /**
     * Sends tracking information related to feedback events.
     *
     * @param actionName The action name for the feedback event.
     * @param value The feedback value.
     *
     * **Event:** feedback
     * **Params.key:** "action_name", "value"
     * **Params.Value:** "feedback", "[...]"
     * **Data type:** string
     * **Description:**
     * - Default action
     * - Feedback value
     * **Trigger:** When the user submits feedback.
     */
    fun sendTrackingFeedback(
        actionName: IKTrackingConst.FeedbackActionName,
        value: String?
    )

    /**
     * Sets custom keys for Crashlytics reporting.
     *
     * @param param Pairs of key-value pairs to be set as custom keys.
     */
    fun setCrashlyticsCustomKeys(vararg param: Pair<String, Any>)
}