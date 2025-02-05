package com.example.ads.activity.listener.keep

import com.example.ads.activity.data.dto.pub.UpdateAppDto

interface SDKNewVersionUpdateCallback {
    fun onUpdateAvailable(updateDto: UpdateAppDto?)
    fun onUpdateFail()
}