package com.example.ads.activity.data.dto.pub

import com.example.ads.activity.utils.IKSdkDefConst

enum class IKAdFormat(var value: String) {
    INTER(IKSdkDefConst.AdFormat.INTER),
    NATIVE(IKSdkDefConst.AdFormat.NATIVE),
    BANNER(IKSdkDefConst.AdFormat.BANNER),
    NATIVE_BANNER(IKSdkDefConst.AdFormat.NATIVE_BANNER),
    REWARD(IKSdkDefConst.AdFormat.REWARD),
    REWARD_INTER(IKSdkDefConst.AdFormat.REWARDED_INTER),
    OPEN(IKSdkDefConst.AdFormat.OPEN),
    BANNER_INLINE(IKSdkDefConst.AdFormat.BANNER_INLINE),
    MREC(IKSdkDefConst.AdFormat.MREC),
    BANNER_COLLAPSE(IKSdkDefConst.AdFormat.BANNER_COLLAPSE),
    BANNER_COLLAPSE_C1(IKSdkDefConst.AdFormat.BANNER_COLLAPSE_C1),
    BANNER_COLLAPSE_C1_BN(IKSdkDefConst.AdFormat.BANNER_COLLAPSE_C1_BN),
    BANNER_COLLAPSE_C1_IL(IKSdkDefConst.AdFormat.BANNER_COLLAPSE_C1_IL),
    AUDIO_ICON(IKSdkDefConst.AdFormat.AUDIO_ICON),
    NATIVE_FULL(IKSdkDefConst.AdFormat.NATIVE_FULL),
    BANNER_COLLAPSE_C2(IKSdkDefConst.AdFormat.BANNER_COLLAPSE_C2),
}