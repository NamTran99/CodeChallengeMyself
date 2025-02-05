package com.example.ads.activity.listener.keep

import com.example.ads.activity.IKSdkConstants

interface IKBillingProvider : SDKIAPProductIDProvider {

    fun licenseKey(): String {
        return IKSdkConstants.LICENSE_KEY
    }

}