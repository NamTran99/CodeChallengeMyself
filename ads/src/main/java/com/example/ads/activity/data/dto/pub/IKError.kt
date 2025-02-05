package com.example.ads.activity.data.dto.pub

import androidx.annotation.Keep

@Keep
data class IKError(val code: Int, val message: String) {
    constructor(dto: Exception?) : this(0, dto?.message ?: "")
    constructor(dto: Throwable?) : this(0, dto?.message ?: "")
}
