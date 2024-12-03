package com.example.myapplication.data.dto

import com.google.gson.annotations.SerializedName

data class VulanTokenRequest(
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("order_id") val orderId: String?,
    @SerializedName("product_id") val productId: String?,
    @SerializedName("purchase_token") val purchaseToken: String?,
    @SerializedName("subscription_id") val subscriptionId: String?
){
}

