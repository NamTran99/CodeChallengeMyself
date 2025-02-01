package com.example.ads.activity.data.dto.pub

/**
 * iapMode:  [subscription], [inapp]
 */
data class SdkTempIapPackageDto(
    val productId: String,
    val productType: String,
    val time:Long
)