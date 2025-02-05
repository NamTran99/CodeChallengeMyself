package com.example.ads.activity.billing.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
class PurchaseData(
    var orderId: String? = null,
    var packageName: String? = null,
    var productId: String? = null,
    var purchaseTime: Date? = null,
    var purchaseState: PurchaseState? = null,
    @Deprecated("Google does not support developer payloads anymore.")
    var developerPayload: String? = null,
    var purchaseToken: String? = null,
    var autoRenewing: Boolean = false,
    var obfuscatedExternalAccountId: String? = null,
    var obfuscatedExternalProfileId: String? = null,
) : Parcelable