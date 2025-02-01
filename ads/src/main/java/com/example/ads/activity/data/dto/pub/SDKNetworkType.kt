package com.example.ads.activity.data.dto.pub

enum class SDKNetworkType(val networkType: String) {
    TypeWifi("wifi"),
    TypeEthernet("ethernet"),
    TypeOther("network_other"),
    TypeMobile3G("mobile_3G"),
    TypeMobile2G("mobile_2G"),
    TypeMobile4G("mobile_4G"),
    TypeMobile5G("mobile_5G"),
    TypeMobileOther("mobile_other"),
    TypeMobileAndroidQ("mobile_Q"),
    NotConnect("not_connect"),
}
