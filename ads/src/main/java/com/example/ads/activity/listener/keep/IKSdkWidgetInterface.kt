package com.example.ads.activity.listener.keep

import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import com.example.ads.activity.data.dto.pub.IKNativeTemplate
import com.example.ads.activity.listener.pub.IKShowWidgetAdListener
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import com.example.ads.activity.widgets.IkmWidgetAdLayout

interface IKSdkWidgetInterface {
    fun loadAd(
        screen: String,
        adListener: IKShowWidgetAdListener?
    )

    fun loadAd(
        screen: String,
        @LayoutRes layoutShimmerRes: Int,
        adListener: IKShowWidgetAdListener?
    )

    fun loadAd(
        @LayoutRes layoutShimmerRes: Int,
        layoutAd: IkmWidgetAdLayout,
        screen: String,
        listener: IKShowWidgetAdListener?
    )

    fun loadAd(
        screen: String,
        template: IKNativeTemplate,
        listener: IKShowWidgetAdListener?
    )

    fun loadAd(
        screen: String,
        template: IKNativeTemplate,
        @LayoutRes layoutShimmerRes: Int,
        listener: IKShowWidgetAdListener?
    )

    fun showWithDisplayAdView(
        screen: String,
        displayWidgetAdView: IkmDisplayWidgetAdView,
        adListener: IKShowWidgetAdListener?
    )

    fun showWithDisplayAdView(
        screen: String,
        displayWidgetAdView: IkmDisplayWidgetAdView,
        @LayoutRes layoutShimmerRes: Int,
        adListener: IKShowWidgetAdListener?
    )

    fun showWithDisplayAdView(
        screen: String,
        template: IKNativeTemplate,
        displayWidgetAdView: IkmDisplayWidgetAdView,
        @LayoutRes layoutShimmerRes: Int,
        adListener: IKShowWidgetAdListener?
    )

    fun showWithDisplayAdView(
        @LayoutRes layoutShimmerRes: Int,
        layoutAd: IkmWidgetAdLayout,
        screen: String,
        displayWidgetAdView: IkmDisplayWidgetAdView,
        adListener: IKShowWidgetAdListener?
    )

    fun attachLifecycle(life: Lifecycle)
    fun loadAdFS(
        layoutAd: IkmWidgetAdLayout,
        screen: String,
        callback: IKShowWidgetAdListener?
    )

    fun reCallLoadAd(listener: IKShowWidgetAdListener?)

    fun loadSAWAd(
        adListener: IKShowWidgetAdListener?
    )

    fun loadAdBySdk(
        screen: String,
        adListener: IKShowWidgetAdListener?
    )


    fun loadAdBySdk(
        screen: String,
        template: IKNativeTemplate,
        @LayoutRes layoutShimmerRes: Int,
        adListener: IKShowWidgetAdListener?
    )


    fun loadAdBySdk(
        screen: String,
        template: IKNativeTemplate,
        adListener: IKShowWidgetAdListener?
    )

    fun loadAdBySdk(
        screen: String,
        @LayoutRes layoutShimmerRes: Int,
        layoutAd: IkmWidgetAdLayout,
        adListener: IKShowWidgetAdListener?
    )

    fun enableFullView()

    fun getIsAdLoaded(): Boolean
    fun getIsAdRecall(): Boolean
    fun getIsAdLoading(): Boolean
    fun getEnableShimmer(): Boolean
    fun setEnableShimmer(value: Boolean)
    fun destroyAd()
    fun setShowCollapseWhenRecall(value: Boolean)
}