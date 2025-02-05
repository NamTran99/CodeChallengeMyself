package com.example.ads.activity.data.dto.sdk

data class IKCustomEventData(
    val p: Int?,
    val unit: String?,
    val time: Long?,
    var isLoading: Boolean = false
) {
    fun getUnitValue(): String {
        return unit ?: ""
    }
    fun getTimeValue(): Long {
        return time ?: 0L
    }
}