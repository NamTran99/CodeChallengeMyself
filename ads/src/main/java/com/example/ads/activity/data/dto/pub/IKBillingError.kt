package com.example.ads.activity.data.dto.pub

import androidx.annotation.Keep
import com.example.ads.activity.data.dto.sdk.IKSdkBillingErrorCode

@Keep
data class IKBillingError(val code: Int, val message: String) {
    constructor(dto: IKSdkBillingErrorCode) : this(dto.code, dto.message)

}
