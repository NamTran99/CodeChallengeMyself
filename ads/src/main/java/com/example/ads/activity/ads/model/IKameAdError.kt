package com.example.ads.activity.ads.model

enum class IKameAdError(val message: String, val code: Int) {
    UNKNOWN("unknown", 4000),
    AD_UNIT_INVALID("adUnitId is null", 4001),
    AD_RESPONSE_INVALID("ad response invalid", 4002),
    PRICE_NOT_VALID("price not valid", 4011),
    ENCRYPT_ERROR("encrypt error", 4003),
    DECRYPT_ERROR("decrypt error", 4004),
    AD_VIEW_TIME_OUT("time out load ad view", 4010),
    AD_VIEW_INVALID("adview invalid", 4012),
    CONTEXT_INVALID("context invalid", 4013),
    AD_NOT_AVAILABLE("ad not available", 4014),
    AD_TIME_OUT("ad timeout", 4010);
}
