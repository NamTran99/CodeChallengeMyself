package com.example.ads.activity.data.dto.pub

import com.example.ads.activity.utils.IKSdkDefConst

enum class IKProductType(val value: String) {
    INAPP(IKSdkDefConst.TXT_PURCHASE),
    SUBS(IKSdkDefConst.TXT_SUBSCRIPTION)
}