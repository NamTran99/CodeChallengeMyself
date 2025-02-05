package com.example.ads.activity.data.dto.pub

import androidx.annotation.Keep
import com.applovin.mediation.MaxError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.example.ads.activity.ads.model.IKameAdError
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode

@Keep
data class IKAdError(val code: Int, val message: String) {
    constructor(dto: IKSdkErrorCode) : this(dto.code, dto.message)
    constructor(dto: LoadAdError) : this(dto.code, dto.message)
    constructor(dto: MaxError) : this(dto.code, dto.message)
    constructor(dto: AdError) : this(dto.code, dto.message)
    constructor(dto: IKameAdError) : this(
        dto.code,
        dto.message
    )

}
