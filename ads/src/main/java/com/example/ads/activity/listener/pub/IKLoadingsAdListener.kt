package com.example.ads.activity.listener.pub

abstract class IKLoadingsAdListener(val timeLoading: Long) {
    abstract fun onShow()
    abstract fun onClose()
}