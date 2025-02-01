//package com.example.ads.activity.widgets
//
//import android.content.Context
//import android.util.AttributeSet
//import android.widget.FrameLayout
//import androidx.annotation.LayoutRes
//import androidx.lifecycle.DefaultLifecycleObserver
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.coroutineScope
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.pub.IKNativeTemplate
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.listener.keep.IKSdkWidgetInterface
//import com.example.ads.activity.listener.pub.IKShowWidgetAdListener
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.withTimeoutOrNull
//
//class IkmWidgetAdView : FrameLayout, IKSdkWidgetInterface {
////    private var adLoader: IKSdkWidgetAdLoader? = null
//
//    constructor(context: Context) : this(context, null) {
//        initViews(context, null)
//    }
//
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
//        initViews(context, attrs)
//    }
//
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
//        context,
//        attrs,
//        defStyleAttr
//    ) {
//        initViews(context, attrs)
//    }
//
//    private fun initViews(context: Context, attrs: AttributeSet?) {
//        if (adLoader != null)
//            return
////        adLoader = IKSdkWidgetAdLoader()
//        adLoader?.initViews(context, attrs, this)
//    }
//
//    override fun loadAd(
//        screen: String,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadAd") { "start load f1" }
//        adLoader?.loadAdCore(screen, adListener)
//    }
//
//    override fun loadAd(
//        screen: String,
//        @LayoutRes layoutShimmerRes: Int,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadAd") { "start load f2" }
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        adLoader?.loadAdCore(screen, adListener)
//    }
//
//    override fun loadAd(
//        @LayoutRes layoutShimmerRes: Int,
//        layoutAd: IkmWidgetAdLayout,
//        screen: String,
//        listener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadAd") { "start load f3" }
////        adLoader?.mAdLayout = layoutAd
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
////        adLoader?.loadAdCore(
////            screen,
////            listener
////        )
//    }
//
//    override fun loadAd(
//        screen: String,
//        template: IKNativeTemplate,
//        listener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadAd") { "start load f4" }
////        adLoader?.nativeTemplate = template
//        adLoader?.loadAdCore(
//            screen,
//            listener
//        )
//    }
//
//    override fun loadAd(
//        screen: String,
//        template: IKNativeTemplate,
//        @LayoutRes layoutShimmerRes: Int,
//        listener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadAd") { "start load f5" }
////        adLoader?.nativeTemplate = template
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        adLoader?.loadAdCore(
//            screen,
//            listener
//        )
//    }
//
//    override fun showWithDisplayAdView(
//        screen: String,
//        displayWidgetAdView: IkmDisplayWidgetAdView,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("showWithDisplayAdView") { "start load f1" }
//        adLoader?.showWithDisplayAdViewCore(screen, displayWidgetAdView, adListener)
//    }
//
//    override fun showWithDisplayAdView(
//        screen: String,
//        displayWidgetAdView: IkmDisplayWidgetAdView,
//        @LayoutRes layoutShimmerRes: Int,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("showWithDisplayAdView") { "start load f2" }
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        adLoader?.showWithDisplayAdViewCore(screen, displayWidgetAdView, adListener)
//    }
//
//    override fun showWithDisplayAdView(
//        screen: String,
//        template: IKNativeTemplate,
//        displayWidgetAdView: IkmDisplayWidgetAdView,
//        @LayoutRes layoutShimmerRes: Int,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("showWithDisplayAdView") { "start load f3" }
////        adLoader?.nativeTemplate = template
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        adLoader?.showWithDisplayAdViewCore(screen, displayWidgetAdView, adListener)
//    }
//
//    override fun showWithDisplayAdView(
//        @LayoutRes layoutShimmerRes: Int,
//        layoutAd: IkmWidgetAdLayout,
//        screen: String,
//        displayWidgetAdView: IkmDisplayWidgetAdView,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("showWithDisplayAdView") { "start load f4" }
////        adLoader?.mAdLayout = layoutAd
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        adLoader?.showWithDisplayAdViewCore(screen, displayWidgetAdView, adListener)
//    }
//
//    override fun attachLifecycle(life: Lifecycle) {
////        adLoader?.uiScope = life.coroutineScope
//        life.addObserver(object : DefaultLifecycleObserver {
//            override fun onDestroy(owner: LifecycleOwner) {
//                super.onDestroy(owner)
////                adLoader?.isParentDestroy = true
//                kotlin.runCatching {
//                    life.removeObserver(this)
//                }
//                destroyAd()
//            }
//        })
//    }
//
//    override fun loadAdFS(
//        layoutAd: IkmWidgetAdLayout,
//        screen: String,
//        callback: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadAdFS") { "start load f1" }
//        adLoader?.loadAdFSCore(layoutAd, screen, callback)
//    }
//
//    override fun reCallLoadAd(listener: IKShowWidgetAdListener?) {
//        adLoader?.showLogD("reCallLoadAd") { "start run" }
//        if (adLoader?.mIsAdLoading == true) {
//            adLoader?.showLogD("reCallLoadAd") { "start fail isAdLoading" }
//            listener?.onAdShowFail(IKAdError(IKSdkErrorCode.CURRENT_AD_LOADING))
//            return
//        }
//        adLoader?.reCallLoadAdCore(listener)
//    }
//
//    override fun loadSAWAd(
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.showLogD("loadSAWAd") { "start load" }
//        adLoader?.loadSAWAdCore(adListener)
//    }
//
//    override fun loadAdBySdk(
//        screen: String,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        adLoader?.uiScope?.launchWithSupervisorJob {
////            val result = withTimeoutOrNull(30000) {
////                while (!isShown) {
////                    delay(1000)
////                }
////                true
////            }
////            if (result == true) {
////                loadAdBySdkCore(screen, adListener)
////            } else {
////                adListener?.onAdShowFail(IKAdError(IKSdkErrorCode.AD_LAYOUT_NOT_SHOWN))
////            }
//        }
//    }
//
//
//    override fun loadAdBySdk(
//        screen: String,
//        template: IKNativeTemplate,
//        @LayoutRes layoutShimmerRes: Int,
//        adListener: IKShowWidgetAdListener?
//    ) {
////        adLoader?.nativeTemplate = template
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        loadAdBySdk(screen, adListener)
//    }
//
//
//    override fun loadAdBySdk(
//        screen: String,
//        template: IKNativeTemplate,
//        adListener: IKShowWidgetAdListener?
//    ) {
////        adLoader?.nativeTemplate = template
//        loadAdBySdk(screen, adListener)
//    }
//
//    override fun loadAdBySdk(
//        screen: String,
//        @LayoutRes layoutShimmerRes: Int,
//        layoutAd: IkmWidgetAdLayout,
//        adListener: IKShowWidgetAdListener?
//    ) {
////        adLoader?.mAdLayout = layoutAd
////        adLoader?.mLoadingAdLayout = layoutShimmerRes
//        loadAdBySdk(screen, adListener)
//    }
//
//    private fun loadAdBySdkCore(
//        screen: String,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        if (adLoader?.mIsAdLoading == true) {
//            return
//        }
//        if (adLoader?.mIsAdLoaded == true) {
//            if (context != null) {
//                visibility = VISIBLE
//                reCallLoadAd(null)
//            }
//            return
//        }
//        kotlin.runCatching {
//            visibility = VISIBLE
//        }
//        adLoader?.loadAdCore(
//            screen,
//            object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    adListener?.onAdShowed()
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    if (arrayOf(
//                            IKSdkErrorCode.AD_LAYOUT_NOT_SHOWN.code,
//                            IKSdkErrorCode.DISABLE_SHOW.code,
//                            IKSdkErrorCode.CONTEXT_NOT_ALIVE.code,
//                            IKSdkErrorCode.CONTEXT_NOT_VALID.code,
//                            IKSdkErrorCode.USER_PREMIUM.code,
//                        ).contains(error.code)
//                    )
//                        adListener?.onAdShowFail(error)
//                    else
//                        adLoader?.loadAdCore(screen + IKSdkDefConst.AdScreen.PREFIX_BACKUP,
//                            object : IKShowWidgetAdListener {
//                                override fun onAdShowed() {
//                                    adListener?.onAdShowed()
//                                }
//
//                                override fun onAdShowFail(error: IKAdError) {
//                                    adListener?.onAdShowFail(error)
//                                }
//
//                                override fun onAdClick() {
//                                    super.onAdClick()
//                                    adListener?.onAdClick()
//                                }
//                            })
//                }
//
//                override fun onAdClick() {
//                    super.onAdClick()
//                    adListener?.onAdClick()
//                }
//            }
//        )
//    }
//
//
//    override fun enableFullView() {
////        adLoader?.mEnableFullView = true
//    }
//
//    override fun getIsAdLoaded(): Boolean {
//        return adLoader?.mIsAdLoaded == true
//    }
//
//    override fun getIsAdRecall(): Boolean {
//        return adLoader?.mIsRecall == true
//    }
//
//    override fun getIsAdLoading(): Boolean {
//        return adLoader?.mIsAdLoading == true
//    }
//
//    override fun getEnableShimmer(): Boolean {
//        return adLoader?.mEnableShimmer == true
//    }
//
//    override fun setEnableShimmer(value: Boolean) {
////        adLoader?.mEnableShimmer = value
//    }
//
//    override fun destroyAd() {
//        adLoader?.destroyCurrentAd()
//        adLoader?.destroyContext()
//        adLoader = null
//    }
//
//    override fun setShowCollapseWhenRecall(value: Boolean) {
////        adLoader?.showCollapseWhenRecal = value
//    }
//}