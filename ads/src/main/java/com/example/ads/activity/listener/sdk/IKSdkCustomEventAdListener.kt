package com.example.ads.activity.listener.sdk

import android.os.Bundle
import com.example.ads.activity.data.dto.pub.IKAdError

interface IKSdkCustomEventAdListener {
    fun onAdLoaded()
    fun onAdLoadFail(error: IKAdError)
    fun onAdClicked()

    fun onAdImpression()

    fun onAdShowed(bundle: Bundle? = null)

    fun onAdShowFailed(error: IKAdError)

    fun onAdDismissed()

    fun onAdsRewarded() {
    }
}