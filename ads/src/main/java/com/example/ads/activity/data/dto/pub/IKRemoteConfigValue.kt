/*
 * Created by CuongNV on 3/2/23, 9:11 AM
 * Copyright (c) by Begamob.com 2023 . All rights reserved.
 * Last modified 3/2/23, 9:11 AM
 */

package com.example.ads.activity.data.dto.pub

import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue

data class IKRemoteConfigValue(var value: String? = "", var source: Int = 0) {


    constructor(value: FirebaseRemoteConfigValue?) : this(value?.asString(), value?.source ?: 0)

    constructor(value: Any? = "", source: Int = 0) : this(value.toString(), source)

    fun getBoolean(): Boolean? = try {
        value?.toBoolean()
    } catch (e: Exception) {
        null
    }

    fun getString(): String? = try {
        value
    } catch (e: Exception) {
        null
    }

    fun getDouble(): Double? = try {
        value?.toDoubleOrNull() ?: 0.0
    } catch (e: Exception) {
        null
    }

    fun getLong(): Long? = try {
        value?.toLongOrNull() ?: 0L
    } catch (e: Exception) {
        null
    }

}
