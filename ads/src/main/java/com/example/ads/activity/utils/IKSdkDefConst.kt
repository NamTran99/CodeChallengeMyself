package com.example.ads.activity.utils

object IKSdkDefConst {

    const val EMPTY = ""
    const val STRING_EMPTY = "empty"
    const val CURRENCY_CODE_USD = "USD"
    const val UNKNOWN = "unknown"
    const val TOTAL_RAM = "total_ram"
    const val CONFIG_OTHER_SUB = "sdk_other_product_id_sub"
    const val CONFIG_OTHER_IN_APP = "sdk_other_product_id_in_app"
    const val USER_PROPERTY_TYPE = "user_type"
    const val FORMAT_DATE_SERVER = "dd/MM/yyyy"

    const val START_APP_TEXT = "start"
    const val TXT_SUBSCRIPTION = "subscription"
    const val TXT_PURCHASE = "inapp"

    const val DATA_RV_DIV_USD = 1_000_000
    const val DATA_RV_D_DIV_USD = 1_000_000.0
    const val THRESHOLD_REV_AD = "threshold_rev_ad"
    const val TOTAL_REV_PUSH_EVENT = "total_rev_push_event"

    const val TIME_AWS_VALID = 240_000
    const val TXT_SCRIPT_LOAD = "load_"
    const val TXT_SCRIPT_SHOW = "show_"
    val cmpCountryCodeList: List<String> by lazy {
        "at,be,bg,hr,cy,cz,dk,ee,fi,fr,de,gr,hu,is,ie,it,lv,li,lt,lu,mt,nl,no,pl,pt,ro,sk,si,es,se,gb,uk,ch"
            .splitToSequence(",").filter { it.isNotEmpty() }.toList()
    }
    const val INTER_SPLASH_COUNTRY_CODE: String = ""
    const val MAX_QUE_LOAD = 10
    const val PLAYGAP_ERROR_CODE = 8999
    const val MIN_AUTO_RELOAD = 5000L
    const val TIME_RECHECK_RELOAD_VIEW = 5000L


    object Config {
        //sdk
        const val SDK_DATA_INTER = "sdk_data_inter"
        const val SDK_DATA_BANNER = "sdk_data_banner"
        const val SDK_DATA_NATIVE = "sdk_data_native"
        const val SDK_DATA_REWARD = "sdk_data_reward"
        const val SDK_DATA_OPEN = "sdk_data_open"
        const val BACK_UP_AD_CONFIG = "sdk_data_default_config"
        const val FIRST_AD_CONFIG = "sdk_data_first_config"
        const val SDK_CUSTOM_NCL = "sdk_data_ncl"
        const val AWS_MEDIATION_CONFIG = "max_mediation_config"
        const val SDK_DATA_OPEN_LC = "sdk_data_o_lc"
        const val SDK_DATA_BANNER_INLINE = "sdk_data_banner_inline"
        const val SDK_DATA_BANNER_COLLAPSE = "sdk_data_bn_cl"
        const val SDK_DATA_BANNER_COLLAPSE_CUSTOM = "sdk_data_bn_cl_custom"
        const val SDK_DATA_NATIVE_FULL_SCREEN = "sdk_data_nt_fs"
        const val SDK_DATA_AUDIO_ICON = "sdk_data_adi"

        const val ENABLE_LOADING_NCL = "enable_loadingNCL"
        const val OTHER_PUR_ID = "other_pur_id"
        const val OTHER_SUB_ID = "other_sub_id"
        const val CACHE_CONFIG = "cache_config"
        const val PURCHASE_USER_IGNORE = "purchase_user_ignore"
        const val UPDATE_APP_CONFIG = "update_app_config"
        const val OTHER_CONFIG_DATA = "other_config_data"
        const val SDK_IAP_CONFIG = "sdk_iap_config"

        //product
        const val CONFIG_DATA_INTER = "prod_data_inter"
        const val CONFIG_DATA_WIDGET = "prod_data_widget"
        const val CONFIG_DATA_REWARD = "prod_data_reward"
        const val CONFIG_DATA_OPEN = "prod_data_open"
        const val REMOTE_VERSION = "remote_version"
        const val CMP_CONFIG_REQUEST_ENABLE = "key_cmp_request_enable"

        const val CONFIG_OTHER_WILL_REMOVE_ADS = "sdk_other_product_id_will_remove_ads"
        const val SDK_USER_PROPERTY_TYPE = "iksdk_user_property_type"
        const val SDK_BACKUP_NATIVE_LATEST = "sdk_bkp_ad_latest"


        var listK = listOf(
            SDK_DATA_INTER, SDK_DATA_BANNER,
            SDK_DATA_NATIVE, SDK_DATA_REWARD,
            SDK_DATA_OPEN, CONFIG_DATA_INTER,
            CONFIG_DATA_WIDGET, CONFIG_DATA_REWARD,
            CONFIG_DATA_OPEN,
            SDK_CUSTOM_NCL, ENABLE_LOADING_NCL,
            OTHER_PUR_ID, OTHER_SUB_ID,
            CACHE_CONFIG, FIRST_AD_CONFIG,
            PURCHASE_USER_IGNORE,
            UPDATE_APP_CONFIG, AWS_MEDIATION_CONFIG,
            CMP_CONFIG_REQUEST_ENABLE, SDK_DATA_BANNER_COLLAPSE,
            SDK_DATA_BANNER_COLLAPSE_CUSTOM,
            BACK_UP_AD_CONFIG,
            SDK_DATA_BANNER_INLINE,
            SDK_DATA_NATIVE_FULL_SCREEN,
            SDK_DATA_AUDIO_ICON,
            ENABLE_LOADING_NCL,
        )

        var exitParamWidget = listOf("exit", "exit_app", "back_app", "exit_dialog")
    }

    object UserPropertyType {
        const val NORMAL = "normal"
        const val PREMIUM = "premium"
        const val REMOVE_ADS = "remove_ads"
    }

    object Sfx {
        const val SPLASH_NAME_SHF = "splash_name_shf"
        const val MSHF = "m_name_sfx"
    }

    object AdFormat {
        const val REWARD = "reward"
        const val REWARDED_INTER = "rewarded_inter"
        const val BANNER = "banner"
        const val NATIVE = "native"
        const val NATIVE_BANNER = "native_banner"
        const val OPEN = "open"
        const val INTER = "inter"
        const val WIDGET = "widget"
        const val MREC = "mrec"
        const val BANNER_COLLAPSE = "banner_collapse"
        const val BANNER_INLINE = "banner_inline"
        const val NATIVE_FULL = "native_full"
        const val AUDIO_ICON = "audio_icon"
        const val BN_CL_BN = "banner_collapse_bn"
        const val BN_CL_BN_IN = "banner_collapse_inline"
        const val BANNER_COLLAPSE_C1 = "banner_collapse_c1"
        const val BANNER_COLLAPSE_C1_BN = "banner_collapse_c1_bn"
        const val BANNER_COLLAPSE_C1_IL = "banner_collapse_c1_il"
        const val BANNER_COLLAPSE_C2 = "banner_collapse_c2"
    }

    object AdAction {
        const val LOAD = "load"
        const val SHOW = "show"
    }

    object AdStatus {
        const val LOAD_FAIL = "load_failed"
        const val CLOSE = "closed"
        const val SHOWED = "showed"
        const val LOADED = "loaded"
        const val CLICKED = "clicked"
        const val IMPRESSION = "impression"
        const val SHOW_FAIL = "show_failed"
        const val START_LOAD = "start_load"
        const val PRE_SHOW = "pre_show"
        const val AD_PLACEMENT = "ad_placement"
        const val REWARDED = "rewarded"
    }

    object Banner {
        const val COLLAPSIBLE = "collapsible"
        const val COLLAPSIBLE_REQUEST_ID = "collapsible_request_id"
        const val TOP = "top"
        const val BOTTOM = "bottom"
    }

    object AdScreen {
        const val BACK_APP = "back_app"
        const val START = "start"
        const val IN_APP = "inapp"
        const val BANNER_SPLASH = "bn_s_adw"
        const val NATIVE_INTER_CUSTOM = "ik_native_ct_it"
        const val NATIVE_INTER_CUSTOM_PG = "ik_native_ct_it_pg"
        const val NATIVE_OPEN_CUSTOM = "ik_native_ct_op"
        const val PREFIX_BACKUP_BANNER_INLINE = "_bup_inline"
        const val PREFIX_BACKUP_BANNER_COLLAPSE = "_bup_cl"
        const val PREFIX_BACKUP = "_backup"
    }

    object AdLabel {
        const val START = "start"
        const val IN_APP = "inapp"
        const val BACKUP = "backup"
        const val PRELOAD = "preload"
        const val NFX = "nfx"
    }

    object NativeAd {
        const val MEDIA_VIEW_ID: Int = 890
        const val HEADLINE_VIEW_ID: Int = 891
        const val BODY_VIEW_ID: Int = 892
        const val CALL_TO_ACTION_VIEW_ID: Int = 893
        const val ICON_VIEW_ID: Int = 894
        const val PRICE_VIEW_ID: Int = 895
        const val STAR_RATING_VIEW_ID: Int = 896
        const val STORE_VIEW_ID: Int = 897
        const val ADVERTISER_VIEW_ID: Int = 898
    }

    object FirstAdType {
        const val INTER = "inter"
        const val OPEN = "open"
    }

    object AdName {
        const val APPLOVIN = "applovin"
        const val FAIR_BID = "fairBid"
        const val IK_AD = "ikAd"
    }

    object TimeOutAd {
        const val INTER = 8//hour
        const val OPEN = 4//hour
        const val BANNER = 8//hour
        const val NATIVE = 8//hour
        const val NATIVE_FULL = 8//hour
        const val REWARD = 8//hour
        const val LOAD_AD_TIME_OUT = 15_000L//millisecond
        const val LOAD_FULL_AD_TIME_OUT = 7_000L//millisecond
        const val LOAD_WIDGET_AD_TIME_OUT = 5_000L//millisecond
    }

    object AdSize {
        const val NORMAL = "normal"
        const val ADAPTIVE = "adaptive"
        const val VIEW_SIZE = "view_size"
        const val VIEW_WIDTH = "view_width"
        const val MANUAL = "manual"
        const val MANUAL_HEIGHT = "manual_height"
        const val MANUAL_HEIGHT_WITH_AD_WIDTH = "manual_height_with_ad_width"
        const val MANUAL_WIDTH = "manual_width"
    }

    object Logs {
        const val SUB_TAG_D = "_d"
        const val SUB_TAG_TRACKING = "_tracking"
        const val SUB_TAG_SDK = "_idk"
        const val SUB_TAG_E = "_e"

        object Level {
            const val LEVEL_1 = "lv1"
            const val LEVEL_TRACKING = "lvTracking"
            const val LEVEL_SDK = "lvSdk"
        }
    }

    object Adjust {
        const val APPLOVIN_MAX_SDK = "applovin_max_sdk"
        const val ADMOB_SDK = "admob_sdk"
        const val IRONSOURCE_SDK = "ironsource_sdk"
        const val ADMOST_SDK = "admost_sdk"
        const val UNITY_SDK = "unity_sdk"
        const val HELIUM_CHARTBOOST_SDK = "helium_chartboost_sdk"
        const val ADX_SDK = "adx_sdk"
        const val PUBLISHER_SDK = "publisher_sdk"
        const val TRADPLUS_SDK = "tradplus_sdk"
        const val TOPON_SDK = "topon_sdk"
        const val MOPUB = "mopub"

        const val TOKEN = "token"
        const val ENVIRONMENT = "environment"
        const val TOKEN_PURCHASE = "token_purchase"
        const val TOKEN_IMPRESSION = "token_impression"
    }

    object MediationTime {
        const val SPLIT_TIME = 200L
        const val TOTAL_TIME = 3000L
    }


    object StartAppStatus {
        const val SHOWED = "showed"
        const val SHOW_FAIL = "show_failed"
        const val START_LOAD = "start_load"
        const val PRE_START_LOAD = "pre_start_load"
        const val START_SHOW = "start_show"
        const val START_SHOW_TIMEOUT = "start_show_timeout"
        const val DISMISS_AD = "dismiss_ad"
        const val FORGE_DISMISS_AD = "forge_dismiss_ad"
        const val PRE_SHOW = "pre_show"
    }

    object NativeBackUpLatest {
        const val ENABLE = "enable"
        const val NATIVE = "native"
        const val BANNER = "banner"
        const val INTER = "inter"
        const val OPEN = "open"
        const val REWARD = "reward"
        const val NATIVE_FULL = "native_full"
        const val BANNER_INLINE = "banner_inline"
        const val NATIVE_BANNER = "native_banner"
        const val COLLAPSE = "collapse"
        const val COLLAPSE_C1 = "collapse_c1"
        const val COLLAPSE_C2 = "collapse_c2"
    }
}