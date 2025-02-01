package com.example.ads.activity.listener.keep

import com.example.ads.activity.data.dto.pub.IKAdjustAttribution

interface OnUserAttributionChangedListener {
    fun onChanged(value: IKAdjustAttribution?)
}