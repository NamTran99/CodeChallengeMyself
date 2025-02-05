package com.example.ads.activity.billing.core

import android.content.Context
import com.example.ads.activity.data.local.IKSdkDataStoreConst

open class BillingCacheBase(val context: Context) {

    val preferencesBaseKey: String
        get() = IKSdkDataStoreConst.Billing.BILLING_DATA_KEY + "_" + context.packageName + "_cache"

}