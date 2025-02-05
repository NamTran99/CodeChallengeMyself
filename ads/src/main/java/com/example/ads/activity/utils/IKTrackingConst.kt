package com.example.ads.activity.utils

import com.example.ads.activity.utils.IKSdkDefConst.AdFormat
import com.example.ads.activity.utils.IKTrackingConst.ParamName.AUDIO
import com.example.ads.activity.utils.IKTrackingConst.ParamName.BANNER
import com.example.ads.activity.utils.IKTrackingConst.ParamName.INTERSTITIAL
import com.example.ads.activity.utils.IKTrackingConst.ParamName.NATIVE
import com.example.ads.activity.utils.IKTrackingConst.ParamName.OPEN_AD
import com.example.ads.activity.utils.IKTrackingConst.ParamName.OTHER
import com.example.ads.activity.utils.IKTrackingConst.ParamName.REWARDED_VIDEO

object IKTrackingConst {
    object ParamName {
        const val STATUS_INTERNET = "status_internet"
        const val AD_FORMAT = "ad_format"
        const val SUB_AD_FORMAT = "sub_ad_format"
        const val AD_POSITION = "placement"
        const val AD_ACTION = "ad_action"
        const val YES = "yes"
        const val NO = "no"
        const val AD_TYPE = "ad_type"
        const val BANNER = "banner"
        const val NATIVE = "native"
        const val REWARDED_VIDEO = "rewarded_video"
        const val INTERSTITIAL = "interstitial"
        const val OPEN_AD = "open_Ad"
        const val AD_PLATFORM = "ad_platform"
        const val AD_PLATFORM_VALUE = "ad_platform_value"
        const val CURRENCY = "currency"
        const val VALUE = "value"
        const val AD_UNIT = "ad_unit_name"
        const val AD_NETWORK = "ad_network"
        const val TIME = "time"
        const val PRIORITY = "priority"
        const val AD_STATUS = "ad_status"
        const val MESSAGE = "message"
        const val ERROR_CODE = "error_code"
        const val SCRIPT_NAME = "script_name"
        const val NETWORK_TYPE = "network_type"
        const val LT_R = "lt_remote"
        const val FA_R = "first_ad_from_remote"
        const val ACTION = "action"
        const val ORDER_ID = "order_id"
        const val PRODUCT_ID = "product_id"
        const val PURCHASE_TIME = "purchase_time"
        const val PRODUCT_TYPE = "product_type"
        const val PURCHASE_TOKEN = "purchase_token"
        const val SDK_VERSION = "sdk_version"
        const val SDK_VERSION_NAME = "sdk_version_name"
        const val REMOTE_VERSION = "remote_version"
        const val RECALL_AD = "recall_ad"
        const val AD_UUID = "ad_custom_id"
        const val AD_ID = "ad_id"
        const val OTHER = "other"
        const val AUDIO = "audio"

        const val SDK_DATA_SOURCE_VERSION = "sdk_dsr_version"
        const val SDK_DATA_SOURCE_VERSION_NAME = "sdk_dsr_version_name"
        const val IS_TIME_OUT = "is_time_out"
    }

    object EventName {
        const val AD_TRACK = "ad_track"
        const val AD_TRACK_NATIVE_LATEST = "ad_track_native_latest"
        const val APP_START_TRACK = "app_start_track"
        const val PURCHASE_SDK_EVENT = "purchase_sdk_event"
        const val CMP_TRACK = "cmp_track"
        const val SDK_VERSION_TRACK = "sdk_version_track"
    }

    fun mapNewAdFormat(adFormat: String): String {
        return when (adFormat) {
            AdFormat.BANNER,
            AdFormat.BANNER_INLINE,
            AdFormat.BANNER_COLLAPSE,
            AdFormat.BANNER_COLLAPSE_C1,
            AdFormat.BANNER_COLLAPSE_C1_BN,
            AdFormat.BANNER_COLLAPSE_C1_IL,
                -> BANNER

            AdFormat.NATIVE,
            AdFormat.NATIVE_BANNER,
            AdFormat.NATIVE_FULL,
            AdFormat.BANNER_COLLAPSE_C2,
                -> NATIVE

            AdFormat.REWARD,
            AdFormat.REWARDED_INTER -> REWARDED_VIDEO

            AdFormat.INTER -> INTERSTITIAL
            AdFormat.OPEN -> OPEN_AD

            AdFormat.AUDIO_ICON -> AUDIO
            else -> OTHER
        }
    }
}