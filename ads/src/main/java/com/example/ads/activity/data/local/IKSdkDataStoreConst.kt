package com.example.ads.activity.data.local

import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.utils.IKSdkDefConst

object IKSdkDataStoreConst {
    const val KEY_LAST_TIME_HANDLE = IkmCoreFMService.KEY_LAST_TIME_HANDLE
    const val MSHF = IKSdkDefConst.Sfx.MSHF
    const val NAME_PREF = "ik_sdk_data_lc"
    const val NAME_BILL_PREF = "ik_sdk_bill_lc"
    const val KEY_TIME_CALL_OPEN_DEFAULT = "time_call_open_default"
    const val ENABLE_LOADING_NCL = "enable_loadingNCL"
    const val KEY_OTHER_CONFIG_DATA = "other_config_data"
    const val AWS_MEDIATION_CONFIG = "aws_mediation_config"
    const val CACHE_ADS_DTO = "cache_ads_dto"
    const val CACHE_ADS_TIME = "cache_ads_time"
    const val CMP_CONFIG_REQUEST_ENABLE = "cmp_config_request_enable"
    const val IK_SDK_DM = "ik_sdk_dm"
    const val REMOTE_CONFIG_DATA = "remote_config_data"
    const val REMOTE_VERSION = "remote_version"
    const val KEY_CURRENT_VERSION_CODE = "key_current_version_code"
    const val BLOCK_TIME_SHOW_FULL_ADS = "block_time_show_full_ads"
    const val KEY_CMP_STATUS = "key_cmp_status"
    const val SPLASH_NAME_SHF = IKSdkDefConst.Sfx.SPLASH_NAME_SHF
    const val LAST_TIME_GET_REMOTE_CONFIG = "last_time_get_remote_config"
    const val FIRST_INIT_ADS_SDK = "first_init_ads_sdk"
    const val SODA_CC = "sodaCc"
    const val THRESHOLD_REV_AD = IKSdkDefConst.THRESHOLD_REV_AD

    object Billing {
        const val BILLING_DATA_KEY = "ik_bl_data_local"
        const val KEY_IAP_PACKAGE = "sdk_iap_package"
        const val IK_TEMP_CACHE_PURCHASE_HISTORY = "ik_temp_cache_purchase_history"
        fun createStringRef(key: String): String {
            return key
        }

        fun createBooleanRef(key: String): String {
            return key
        }
    }

}