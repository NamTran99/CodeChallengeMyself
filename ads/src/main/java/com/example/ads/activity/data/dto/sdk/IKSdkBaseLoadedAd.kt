package com.example.ads.activity.data.dto.sdk

import com.applovin.mediation.ads.MaxAdView
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.nativead.NativeAd
import com.example.ads.activity.data.dto.sdk.data.IKAdSizeDto
import com.example.ads.activity.listener.sdk.IKAdActionCallback
import com.example.ads.activity.listener.sdk.IKAdActionCallbackObj
import com.example.ads.activity.mediation.applovin.IKApplovinHelper
import com.example.ads.activity.mediation.applovin.IkObjectNativeMax

data class IKSdkBaseLoadedAd<T>(
    var unitId: String? = null,
    var loadedAd: T? = null,
    var adPriority: Int = 0,
    var showPriority: Int = 0,
    var lastTimeLoaded: Long = 0L,
    var adFormat: String? = ""
) {
    var listener: IKAdActionCallback<T, Any>? = null
    var listener2: IKAdActionCallbackObj<T, Any>? = null
    var adNetwork: String = "normal"
    var des = ""
    var uuid: String = kotlin.runCatching {
        ""
    }.getOrNull() ?: ""
    var isRemove = false
    var isDisplayAdView = false
    var isBackup = false
    var adSizeDto: IKAdSizeDto? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IKSdkBaseLoadedAd<*>

        if (unitId != other.unitId) return false
        if (loadedAd != other.loadedAd) return false
        if (adPriority != other.adPriority) return false
        if (showPriority != other.showPriority) return false
        if (lastTimeLoaded != other.lastTimeLoaded) return false
        if (adFormat != other.adFormat) return false
        if (listener != other.listener) return false
        if (adNetwork != other.adNetwork) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unitId?.hashCode() ?: 0
        result = 31 * result + (loadedAd?.hashCode() ?: 0)
        result = 31 * result + adPriority
        result = 31 * result + showPriority
        result = 31 * result + lastTimeLoaded.hashCode()
        result = 31 * result + (adFormat?.hashCode() ?: 0)
        result = 31 * result + (listener?.hashCode() ?: 0)
        result = 31 * result + adNetwork.hashCode()
        return result
    }

    fun removeListener() {
        if (isDisplayAdView || isBackup) {
            return
        }
        listener = null
    }

    fun destroyObject() {
        if (isDisplayAdView || isBackup) {
            return
        }
        listener = null
        listener2 = null
        loadedAd = null
    }

    fun destroyAd() {
        if (isDisplayAdView || isBackup) {
            return
        }
        when {
            loadedAd is NativeAd -> {
                runCatching {
                    (loadedAd as? NativeAd)?.destroy()
                }
            }

            loadedAd is AdView -> {
                runCatching {
                    (loadedAd as? AdView)?.destroy()
                }
            }

            loadedAd is AdManagerAdView -> {
                runCatching {
                    (loadedAd as? AdManagerAdView)?.destroy()
                }
            }

//            IKApplovinHelper.isBannerAd(loadedAd) -> {
//                runCatching {
//                    (loadedAd as? MaxAdView)?.destroy()
//                }
//            }

//            IKApplovinHelper.isNativeAd(loadedAd) -> {
//                runCatching {
//                    (loadedAd as? IkObjectNativeMax)?.apply {
//                        this.loader.destroy()
//                    }
//                }
//            }

            else -> {

            }
        }
        destroyObject()
    }
}