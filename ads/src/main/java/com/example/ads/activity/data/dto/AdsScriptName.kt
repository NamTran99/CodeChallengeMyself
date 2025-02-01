package com.example.ads.activity.data.dto

enum class AdsScriptName(var value: String) {
    OPEN_ADMOB_NORMAL("open_admob_normal"),
    OPEN_ADMOB_DEFAULT_BID1("open_admob_default_floor1"),
    OPEN_ADMOB_DEFAULT_BID2("open_admob_default_floor2"),

    SHOW_OPEN_ADMOB_NORMAL("show_open_admob_normal"),
    SHOW_OPEN_ADMOB_BID("show_open_admob_floor"),

    SHOW_OPEN_ADMANAGER_NORMAL("show_open_admanager_normal"),
    SHOW_OPEN_ADMANAGER_BID("show_open_admanager_floor"),


    OPEN_MAX_NORMAL("open_max_normal"),
    OPEN_MAX_BACKUP(""),
    OPEN_ADMOB_BACKUP(""),
    OPEN_ADMANAGER_NORMAL(""),
    OPEN_ADMANAGER_FLOOR(""),
    OPEN_ADMANAGER_BACKUP(""),

    BANNER_ADMOB_NORMAL("banner_admob_normal"),
    BANNER_NORMAL("banner_normal"),
    BANNER_ADMOB_COLLAPSIBLE("banner_admob_collapsible"),
    BANNER_ADMANAGER_NORMAL("banner_admanager_normal"),
    BANNER_MAX_NORMAL("banner_max_normal"),
    BANNER_FAN_NORMAL("banner_fan_normal"),
    BANNER_IRON_NORMAL("banner_iron_normal"),
    BANNER_YANDEX_NORMAL("banner_yandex_normal"),

    BANNER_ADMOB_BACKUP("banner_admob_backup"),
    BANNER_ADMANAGER_BACKUP("banner_admanager_backup"),
    BANNER_MAX_BACKUP("banner_max_backup"),
    BANNER_FAN_DEFAULT("banner_fan_default"),
    BANNER_IRON_DEFAULT("banner_iron_default"),

    BANNER_ADMOB_FLOOR("banner_admob_floor"),
    BANNER_ADMANAGER_FLOOR("banner_admanager_floor"),

    NATIVE_ADMOB_NORMAL("native_admob_normal"),
    NATIVE_YANDEX_NORMAL("native_yandex_normal"),
    NATIVE_NORMAL("native_normal"),
    NATIVE_ADMANAGER_NORMAL("native_admanager_normal"),
    NATIVE_ADMANAGER_FLOOR("native_admanager_floor"),
    NATIVE_MAX_NORMAL("native_max_normal"),
    NATIVE_FAN_NORMAL("native_fan_normal"),
    NATIVE_IRON_NORMAL("native_iron_normal"),

    NATIVE_ADMOB_FLOOR("native_admob_floor"),
    NATIVE_PRELOAD("native_preload"),
    NATIVE_ADMOB_BACKUP("native_admob_backup"),
    NATIVE_BACKUP("native_backup"),
    NATIVE_ADMANAGER_BACKUP("native_admanager_backup"),
    NATIVE_MAX_BACKUP("native_max_backup"),
    NATIVE_FAN_BACKUP("native_fan_backup"),
    NATIVE_IRON_BACKUP("native_iron_backup"),

    NATIVE_CUSTOM_ADMOB_NORMAL("native_custom_admob_normal"),
    NATIVE_CUSTOM_ADMANAGER_NORMAL("native_custom_admanager_normal"),
    NATIVE_CUSTOM_FAN_NORMAL("native_custom_fan_normal"),

    NATIVE_CUSTOM_ADMOB_FLOOR("native_custom_admob_floor"),

    NATIVE_CUSTOM_ADMOB_BACKUP("native_custom_admob_backup"),
    NATIVE_CUSTOM_BACKUP("native_custom_backup"),
    NATIVE_CUSTOM_FULL_BACKUP("native_custom_full_backup"),
    NATIVE_CUSTOM_ADMANAGER_BACKUP("native_custom_admanager_backup"),
    NATIVE_CUSTOM_MAX_BACKUP("native_custom_max_backup"),
    NATIVE_CUSTOM_FAN_BACKUP("native_custom_fan_backup"),
    NATIVE_CUSTOM_IRON_BACKUP("native_custom_iron_backup"),

    NATIVE_FULL_ADMOB_NORMAL("native_full_admob_normal"),

    INTERSTITIAL_START_NORMAL("interstitial_start_normal"),
    INTERSTITIAL_ADMOB_START_NORMAL("interstitial_admob_start_normal"),
    INTERSTITIAL_ADMANAGER_START_NORMAL("interstitial_admanager_start_normal"),
    INTERSTITIAL_MAX_START_NORMAL("interstitial_max_start_normal"),
    INTERSTITIAL_IRON_START_NORMAL("interstitial_iron_start_normal"),
    INTERSTITIAL_FAN_START_NORMAL("interstitial_fan_start_normal"),
    INTERSTITIAL_YANDEX_START_NORMAL("interstitial_yandex_start_normal"),
    INTERSTITIAL_APS_START_NORMAL("interstitial_aps_start_normal"),
    INTERSTITIAL_MINTEGRAL_START_NORMAL("interstitial_mintegral_start_normal"),

    INTERSTITIAL_INAPP_NORMAL("interstitial_inapp_normal"),
    INTERSTITIAL_ADMOB_INAPP_NORMAL(""),
    INTERSTITIAL_ADMANAGER_INAPP_NORMAL("interstitial_admanager_inapp_normal"),
    INTERSTITIAL_MAX_INAPP_NORMAL("interstitial_max_inapp_normal"),
    INTERSTITIAL_IRON_INAPP_NORMAL("interstitial_iron_inapp_normal"),
    INTERSTITIAL_FAN_INAPP_NORMAL("interstitial_fan_inapp_normal"),
    INTERSTITIAL_YANDEX_INAPP_NORMAL("interstitial_yandex_inapp_normal"),
    INTERSTITIAL_APS_INAPP_NORMAL("interstitial_aps_inapp_normal"),
    INTERSTITIAL_MINTEGRAL_INAPP_NORMAL("interstitial_mintegral_inapp_normal"),

    INTERSTITIAL_EXIT_NORMAL("interstitial_exit_normal"),
    INTERSTITIAL_ADMOB_EXIT_NORMAL("interstitial_admob_exit_normal"),
    INTERSTITIAL_ADMANAGER_EXIT_NORMAL("interstitial_admanager_exit_normal"),
    INTERSTITIAL_MAX_EXIT_NORMAL("interstitial_max_exit_normal"),
    INTERSTITIAL_IRON_EXIT_NORMAL("interstitial_iron_exit_normal"),
    INTERSTITIAL_FAN_EXIT_NORMAL("interstitial_fan_exit_normal"),
    INTERSTITIAL_OTHER_NORMAL("interstitial_other_normal"),

    INTERSTITIAL_DEFAULT_NORMAL("interstitial_default_normal"),
    INTERSTITIAL_ADMOB_DEFAULT_NORMAL("interstitial_admob_default_normal"),
    INTERSTITIAL_YANDEX_DEFAULT_NORMAL("interstitial_yandex_default_normal"),
    INTERSTITIAL_APS_DEFAULT_NORMAL("interstitial_aps_default_normal"),
    INTERSTITIAL_MINTEGRAL_DEFAULT_NORMAL("interstitial_mintegral_default_normal"),
    INTERSTITIAL_ADMANAGER_DEFAULT_NORMAL("interstitial_admanager_default_normal"),
    INTERSTITIAL_MAX_DEFAULT_NORMAL("interstitial_max_default_normal"),
    INTERSTITIAL_IRON_DEFAULT_NORMAL("interstitial_iron_default_normal"),
    INTERSTITIAL_FAN_DEFAULT_NORMAL("interstitial_fan_default_normal"),

    INTERSTITIAL_CUSTOM_NORMAL("interstitial_custom_normal"),
    INTERSTITIAL_ADMOB_CUSTOM_NORMAL("interstitial_admob_custom_normal"),
    INTERSTITIAL_ADMANAGER_CUSTOM_NORMAL("interstitial_admanager_custom_normal"),
    INTERSTITIAL_MAX_CUSTOM_NORMAL("interstitial_max_custom_normal"),
    INTERSTITIAL_IRON_CUSTOM_NORMAL("interstitial_iron_custom_normal"),
    INTERSTITIAL_FAN_CUSTOM_NORMAL("interstitial_fan_custom_normal"),

    INTERSTITIAL_ADMOB_FLOOR("interstitial_admob_floor"),
    INTERSTITIAL_ADMOB_MEDIATION("interstitial_admob_mediation"),
    INTERSTITIAL_FAN_BIDDY("interstitial_fan_biddy"),

    REWARDED_ADMOB_NORMAL("rewarded_admob_normal"),
    REWARDED_ADMANAGER_NORMAL("rewarded_admanager_normal"),
    REWARDED_MAX_NORMAL("rewarded_max_normal"),
    REWARDED_IRON_NORMAL("rewarded_iron_normal"),

    REWARD_ADMANAGER_BACKUP(""),
    REWARDED_ADMANAGER_MEDIATION("rewarded_admanager_mediation"),
    REWARD_MAX_BACKUP(""),
    REWARDED_IRON_BACKUP("rewarded_iron_backup"),

    REWARDED_ADMOB_MEDIATION("rewarded_admob_mediation"),
    REWARDED_ADMOB_FLOOR("rewarded_admob_floor"),
    INTERSTITIAL_IKAD_INAPP_NORMAL("interstitial_ikad_inapp_normal"),
    REWARD_ADMOB_BACKUP(""), ;


}