package com.example.ads.activity.format.open_ads

import android.app.Activity
import android.content.Context
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.coroutines.CoroutineScope

abstract class IKSdkBaseAppOpenAd<T : Any>(adNetwork: AdNetwork) :
    IKSdkBaseAd<T>(adNetwork) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.OPEN
    override val logTag: String
        get() = "${adFormatName}_$adNetworkName"

    abstract suspend fun showAd(
        coroutineScope: CoroutineScope,
        activity: Activity,
        screen: String,
        adsListener: IKSdkBaseListener
    )

    override fun loadPreloadAd(
        coroutineScope: CoroutineScope,
        context: Context,
        adData: IKAdapterDto,
        scriptName: String,
        callback: IKSdkLoadAdCoreListener
    ) {

    }
}