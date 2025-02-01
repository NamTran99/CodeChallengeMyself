package com.example.ads.activity.format.intertial

import android.app.Activity
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.coroutines.CoroutineScope

abstract class IKSdkBaseInterstitialAds<T : Any>(adNetwork: AdNetwork) :
    IKSdkBaseAd<T>(adNetwork) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.INTER
    override val logTag: String
        get() = "${adFormatName}_$adNetworkName"

    abstract fun showAd(
        coroutineScope: CoroutineScope,
        activity: Activity,
        screen: String,
        scriptName: String,
        adsListener: IKSdkBaseListener
    )

}