package com.example.ads.activity.data.dto.pub

object IKTrackingConst {
    enum class PermissionActionName(val value: String) {
        POPUP("popup"),
        GRANT_PERMISSION("grant_permission"),
        CHECK_PERMISSION("check_permission")
    }

    enum class PermissionStatus(val value: String) {
        YES("yes"),
        NO("no")
    }

    enum class NotificationActionName(val value: String) {
        SHOW("show"),
        CLICK("click")
    }

    enum class FeedbackActionName(val value: String) {
        FEEDBACK("feedback"),
        RATING("rating")
    }
}