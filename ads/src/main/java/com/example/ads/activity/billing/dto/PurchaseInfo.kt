package com.example.ads.activity.billing.dto

import android.os.Parcelable
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Date

@Parcelize
data class PurchaseInfo(
    var responseData: String? = null,
    var signature: String? = null,
    var purchaseData: PurchaseData? = null,
    var productType: String? = null
) : Parcelable {

    constructor(responseData: String?, signature: String?, productType: String?) : this() {
        this.responseData = responseData
        this.signature = signature
        this.productType = productType
        purchaseData = parseResponseDataImpl()
    }

    fun getPurchaseDataDetail(): PurchaseData? {
        return purchaseData
    }

    private fun parseResponseDataImpl(): PurchaseData? {
        return try {
            val json = JSONObject(responseData ?: IKSdkDefConst.EMPTY)
            val data = PurchaseData()
            data.orderId = json.optString(IkSdkBillingConst.RESPONSE_ORDER_ID)
            data.packageName = json.optString(IkSdkBillingConst.RESPONSE_PACKAGE_NAME)
            data.productId = json.optString(IkSdkBillingConst.RESPONSE_PRODUCT_ID)
            val purchaseTimeMillis = json.optLong(IkSdkBillingConst.RESPONSE_PURCHASE_TIME, 0)
            data.purchaseTime = if (purchaseTimeMillis != 0L) Date(purchaseTimeMillis) else null
            data.purchaseState =
                PurchaseState.values()[json.optInt(IkSdkBillingConst.RESPONSE_PURCHASE_STATE, 1)]
            data.purchaseToken = json.getString(IkSdkBillingConst.RESPONSE_PURCHASE_TOKEN)
            data.autoRenewing = json.optBoolean(IkSdkBillingConst.RESPONSE_AUTO_RENEWING)
            data.obfuscatedExternalAccountId =
                json.optString(IkSdkBillingConst.OBFUSCATED_EXTERNAL_ACCOUNT_ID)
            data.obfuscatedExternalProfileId =
                json.optString(IkSdkBillingConst.OBFUSCATED_EXTERNAL_PROFILE_ID)
            data
        } catch (e: Exception) {
            null
        }
    }


}