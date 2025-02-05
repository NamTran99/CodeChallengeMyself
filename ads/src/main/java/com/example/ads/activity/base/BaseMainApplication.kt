package com.example.ads.activity.base

import android.app.Application
import com.example.ads.activity.billing.IKBillingManager
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.listener.keep.SDKIAPProductIDProvider

abstract class BaseMainApplication : Application(){

    companion object {
        private lateinit var instance: BaseMainApplication

        fun getInstance(): BaseMainApplication? {
            return if (::instance.isInitialized) instance else null
        }

    }

    private val billingManager= IKBillingManager()
    // Được gọi khi ứng dụng khởi chạy
    override fun onCreate() {
        super.onCreate()
        // Logic khởi tạo ứng dụng
        instance = this

        IKSdkApplicationProvider.init(this)
        billingManager.initBilling(this ,configIAPData())
    }

    // Phương thức abstract bắt buộc lớp con phải triển khai
    abstract fun configIAPData(): SDKIAPProductIDProvider

    open fun enableRewardAd(): Boolean {
        return false
    }
}