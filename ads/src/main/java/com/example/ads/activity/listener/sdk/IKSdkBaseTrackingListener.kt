package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKTrackingConst

abstract class IKSdkBaseTrackingListener(
    var screen: String,
    var adFormat: String,
    var adNetworkName: String,
    var scriptName: String?,
    var isRecall: Boolean = false
) : IKSdkBaseListener() {
    override fun onAdClicked(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    ) {
        if (adNetworkName.isNotBlank())
            this.adNetworkName = adNetworkName
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat,
            IKSdkDefConst.AdStatus.CLICKED,
            this.screen,
            Pair(IKTrackingConst.ParamName.AD_NETWORK, this.adNetworkName),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(IKTrackingConst.ParamName.AD_UUID, adUUID),
            Pair(
                IKTrackingConst.ParamName.RECALL_AD,
                if (isRecall) IKTrackingConst.ParamName.YES else IKTrackingConst.ParamName.NO
            )
        )
    }

    override fun onAdImpression(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    ) {
        if (adNetworkName.isNotBlank())
            this.adNetworkName = adNetworkName
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat,
            IKSdkDefConst.AdStatus.IMPRESSION,
            this.screen,
            Pair(IKTrackingConst.ParamName.AD_NETWORK, this.adNetworkName),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(IKTrackingConst.ParamName.AD_UUID, adUUID),
            Pair(
                IKTrackingConst.ParamName.RECALL_AD,
                if (isRecall) IKTrackingConst.ParamName.YES else IKTrackingConst.ParamName.NO
            )
        )
    }

    override fun onAdDismissed(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    ) {
        if (adNetworkName.isNotBlank())
            this.adNetworkName = adNetworkName
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat,
            IKSdkDefConst.AdStatus.CLOSE,
            this.screen,
            Pair(IKTrackingConst.ParamName.AD_NETWORK, this.adNetworkName),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(IKTrackingConst.ParamName.AD_UUID, adUUID),
            Pair(
                IKTrackingConst.ParamName.RECALL_AD,
                if (isRecall) IKTrackingConst.ParamName.YES else IKTrackingConst.ParamName.NO
            )
        )
    }

    override fun onAdShowFailed(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        error: IKAdError
    ) {
        if (adNetworkName.isNotBlank())
            this.adNetworkName = adNetworkName
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat,
            IKSdkDefConst.AdStatus.SHOW_FAIL,
            this.screen,
            Pair(IKTrackingConst.ParamName.AD_NETWORK, this.adNetworkName),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}")
        )
    }

    override fun onAdShowed(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        priority: Int,
        adUUID: String
    ) {
        if (adNetworkName.isNotBlank())
            this.adNetworkName = adNetworkName
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat,
            IKSdkDefConst.AdStatus.SHOWED,
            this.screen,
            Pair(IKTrackingConst.ParamName.AD_NETWORK, this.adNetworkName),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(IKTrackingConst.ParamName.AD_UUID, adUUID),
            Pair(
                IKTrackingConst.ParamName.RECALL_AD,
                if (isRecall) IKTrackingConst.ParamName.YES else IKTrackingConst.ParamName.NO
            )
        )
    }


    override fun onAdsRewarded(
        adNetworkName: String,
        screen: String,
        scriptName: String,
        adUUID: String
    ) {
        if (adNetworkName.isNotBlank())
            this.adNetworkName = adNetworkName
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat,
            IKSdkDefConst.AdStatus.REWARDED,
            this.screen,
            Pair(IKTrackingConst.ParamName.AD_NETWORK, this.adNetworkName),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(IKTrackingConst.ParamName.AD_UUID, adUUID),
            Pair(
                IKTrackingConst.ParamName.RECALL_AD,
                if (isRecall) IKTrackingConst.ParamName.YES else IKTrackingConst.ParamName.NO
            )
        )
    }

    fun onAdPreShow(screen: String, scriptName: String) {
        if (screen.isNotBlank())
            this.screen = screen
        if (scriptName.isNotBlank())
            this.scriptName = scriptName
        IKSdkTrackingHelper.trackingSdkShowAd(
            adFormat = adFormat,
            adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
            screen = this.screen,
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, this.scriptName ?: IKSdkDefConst.UNKNOWN),
            Pair(
                IKTrackingConst.ParamName.RECALL_AD,
                if (isRecall) IKTrackingConst.ParamName.YES else IKTrackingConst.ParamName.NO
            )
        )
    }

}