package com.example.ads.activity.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.AdaptiveIconDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.multidex.MultiDexApplication
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.CommonNotificationBuilder
import com.google.firebase.messaging.Constants
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.NotificationParams
import com.google.firebase.messaging.ktx.messaging
//import com.ikame.android.datasourceadapter.IKDataSourceHelper
//import com.example.ads.activity.BuildConfig
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.IKSdkController
import com.example.ads.activity.IKSdkOptions
import com.example.ads.R
//import com.example.ads.activity.ads.2IKameAdController
import com.example.ads.activity.core.IKDataCoreManager
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdjustAttribution
import com.example.ads.activity.data.dto.pub.SDKNetworkType
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.data.local.IKSdkDataStoreCore
//import com.example.ads.activity.format.banner.IKBannerInlineController
//import com.example.ads.activity.format.intertial.IKInterController
//import com.example.ads.activity.format.open_ads.IKAppOpenController
//import com.example.ads.activity.format.rewarded.IKRewardedController
//import com.example.ads.activity.ik_log.IKSdkConnectDataCallback
//import com.example.ads.activity.ik_log.IKSdkConnectInterface
import com.example.ads.activity.listener.SDKLifecycleCallback
import com.example.ads.activity.listener.SDKLifecycleObserver
import com.example.ads.activity.listener.keep.OnUserAttributionChangedListener
import com.example.ads.activity.listener.pub.IKAppOpenAdCallback
import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
import com.example.ads.activity.mediation.applovin.IKApplovinHelper
//import com.example.ads.activity.tracking.CoreTracking
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.tracking.IKTrackingHelper
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IkmSdkCoreFunc.SdkF.mContainAdActivity
import com.example.ads.activity.utils.IkmSdkCoreFunc.Utils.containAdsActivity
import com.ironsource.mediationsdk.IronSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean


object IkmSdkCoreFunc {

    object Utils {
        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL

            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else capitalize(manufacturer) + " " + model
        }

        private fun capitalize(str: String): String {
            if (TextUtils.isEmpty(str)) {
                return str
            }

            val arr = str.toCharArray()
            var capitalizeNext = true
            val phrase = StringBuilder()
            runCatching {
                for (c in arr) {
                    if (capitalizeNext && Character.isLetter(c)) {
                        phrase.append(Character.toUpperCase(c))
                        capitalizeNext = false
                        continue
                    } else if (Character.isWhitespace(c)) {
                        capitalizeNext = true
                    }
                    phrase.append(c)
                }
            }
            return phrase.toString()
        }

        fun getActivityInfo(context: Context?, flag: Int): PackageInfo? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context?.packageManager?.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(flag.toLong())
                )
            } else {
                @Suppress("DEPRECATION") context?.packageManager?.getPackageInfo(
                    context.packageName,
                    flag
                )
            }
        }

        suspend fun containAdsActivity(): Boolean {
            val currentActivity = kotlin.runCatching {
                AppF.filterActivityElements { it.value != null }?.values?.lastOrNull()
            }.getOrNull()
            if (currentActivity == null)
                return false
            val iterator = mContainAdActivity.iterator()
            var activityFound = false
            while (iterator.hasNext()) {
                val activityName = kotlin.runCatching {
                    iterator.next()
                }.getOrNull() ?: break
                if (currentActivity.javaClass.name.contains(activityName, true)) {
                    activityFound = true
                    break
                }
            }
            return activityFound
        }
    }

    object AppF {
        private var mUserUid = IKSdkDefConst.UNKNOWN
        var listActivity: LinkedHashMap<String, Activity?> = linkedMapOf()
        private val mutexActivity = Mutex()
        private val activityScope = CoroutineScope(Dispatchers.Main)
        var mActivityEnableShowResumeAd: ArrayList<Class<*>> = arrayListOf()
        var mIsLoadingAds = false
        var mIKAppOpenAdCallback: IKAppOpenAdCallback? = null
        var mSDKNetworkType: SDKNetworkType = SDKNetworkType.TypeOther

        private var connectivityManager: ConnectivityManager? = null
        var isInternetAvailable: Boolean = true

        private var networkConnectivityCallback: ConnectivityManager.NetworkCallback? = null
        var mCustomOnNetworkConnectivityCallback: ArrayList<ConnectivityManager.NetworkCallback> =
            arrayListOf()
        var mListActLifecycleCallbacks: ArrayList<Application.ActivityLifecycleCallbacks> =
            arrayListOf()
        var mAttributionChangedListener: OnUserAttributionChangedListener? = null
        var isAppLive = true
            private set(value) {
                field = value
                isAppLiveLoad = field
            }
        var isAppLiveLoad = false
            private set
        private var isUserIdLoaded = AtomicBoolean(false)


        suspend fun filterActivityElements(predicate: (Map.Entry<String, Activity?>) -> Boolean): Map<String, Activity?>? {
            return try {
                listActivity.filter(predicate)
            } catch (e: Exception) {
                null
            }
        }

        @Suppress("DEPRECATION")
        fun getCurrentDisplay(context: Context): Display? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else ((context as? Activity)
                ?: listActivity.values.lastOrNull())?.windowManager?.defaultDisplay
        }

        private suspend fun showResumeOpenAd(activity: Activity) {
            showLogD("showResumeAd") { "start run" }
            if (mIsLoadingAds) {
                showLogSdk("showResumeAd") { "is loading ads -> block show" }
                return
            }
            mIsLoadingAds = true
            mIKAppOpenAdCallback?.onAdLoading()
            showLogD("showResumeAd") { "start showing" }
//            IKAppOpenController.showAd(activity,
//                screen = IKSdkOptions.openAdResumeTrackingScreen.ifBlank { IKSdkDefConst.AdScreen.IN_APP },
//                object : IKSdkShowAdListener {
//                    override fun onAdShowed(priority: Int) {
//                        showLogD("showResumeAd") { "onAdsShowed" }
//                        mIsLoadingAds = false
//                        mIKAppOpenAdCallback?.onShowAdComplete()
//                    }
//
//                    override suspend fun onAdReady(priority: Int) {
//                    }
//
//                    override fun onAdDismiss() {
//                        mIsLoadingAds = false
//                        showLogD("showResumeAd") { "onAdsDismiss" }
//                        mIKAppOpenAdCallback?.onAdDismiss()
//                    }
//
//                    override fun onAdShowFail(error: IKAdError) {
//                        mIKAppOpenAdCallback?.onShowAdFail()
//                        mIsLoadingAds = false
//                        showLogD("showResumeAd") { "onAdsShowFail code=$error" }
//                    }
//
//                    override fun onAdShowTimeout() {
//                        mIKAppOpenAdCallback?.onAdsShowTimeout()
//                        mIsLoadingAds = false
//                        showLogD("showResumeAd") { "onAdsShowTimeout" }
//                    }
//                })
//
        }

        fun handleNetwork(context: Context) {
            mSDKNetworkType = IKSdkUtilsCore.isInternetAvailable(context)
            isInternetAvailable = mSDKNetworkType != SDKNetworkType.NotConnect
            connectivityManager =
                context.getSystemService(MultiDexApplication.CONNECTIVITY_SERVICE) as? ConnectivityManager
            networkConnectivityCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    isInternetAvailable = true
                    mCustomOnNetworkConnectivityCallback.forEach {
                        it.onAvailable(network)
                    }

                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    isInternetAvailable = false
                    mCustomOnNetworkConnectivityCallback.forEach {
                        it.onLost(network)
                    }
                }
            }
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    val request = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                    networkConnectivityCallback?.let {
                        connectivityManager?.registerNetworkCallback(request, it)
                    }
                } else {
                    networkConnectivityCallback?.let {
                        connectivityManager?.registerDefaultNetworkCallback(it)
                    }
                }
            }
        }

        private suspend fun initAdjust(context: Context) {
            coroutineScope {
                launchWithSupervisorJob(Dispatchers.IO) {
                    //Adjust
//                    val config = AdjustConfig(
//                        context,
//                        IKSdkConstants.ADJUST_TOKEN.trim(),
//                        IKSdkConstants.ADJUST_ENVIRONMENT
//                    )
//                    kotlin.runCatching {
//                        config.fbAppId =
//                            context.getString(IKDataSourceHelper.getFacebookApplicationId())
//                    }
//                    IKLogs.dNoneSdk("AdjustConfig") {
//                        "token = ${IKSdkConstants.ADJUST_TOKEN.trim()}," +
//                                " environment = ${IKSdkConstants.ADJUST_ENVIRONMENT}"
//                    }
//                    showLogD("AdjustConfig") {
//                        "token = ${IKSdkConstants.ADJUST_TOKEN.trim()}," +
//                                " environment = ${IKSdkConstants.ADJUST_ENVIRONMENT}"
//                    }
//                    config.setOnAttributionChangedListener { attribution ->
//                        mAttributionChangedListener?.onChanged(IKAdjustAttribution().apply {
//                            trackerToken = attribution.trackerToken
//                            trackerName = attribution.trackerName
//                            network = attribution.network
//                            campaign = attribution.campaign
//                            adgroup = attribution.adgroup
//                            creative = attribution.creative
//                            clickLabel = attribution.clickLabel
//                            costType = attribution.costType
//                            costAmount = attribution.costAmount
//                            costCurrency = attribution.costCurrency
//                            fbInstallReferrer = attribution.fbInstallReferrer
//                        })
//                        showLogD("AdjustConfig") {
//                            "OnAttributionChangedListener = $attribution"
//                        }
//                    }
//                    Adjust.initSdk(config)
                }
                launchWithSupervisorJob(Dispatchers.IO) {
                    Adjust.getGoogleAdId(context) { id ->
                        context.let { FirebaseAnalytics.getInstance(it) }.setUserId(id)
                    }
                }

                launchWithSupervisorJob(Dispatchers.IO) {
                    runCatching {
                        Adjust.getAdid { adId ->
                            runCatching {
                                showLogSdk("AdjustConfig") {
                                    "setAdjustID null"
                                }
                                if (adId.isNullOrBlank())
                                    return@getAdid
                                showLogSdk("AdjustConfig") {
                                    "setAdjustID ready"
                                }
                                if (isUserIdLoaded.compareAndSet(false, true)) {
                                    FirebaseAnalytics.getInstance(context).setUserId(adId)
                                    kotlin.runCatching {
                                        IronSource.setUserId(adId)
                                    }
//                                    CoreTracking.logUserId(adId)
                                    runCatching {
                                        IKSdkDataStoreCore.putString("user_id", adId)
                                    }
                                    showLogSdk("AdjustConfig") {
                                        "setAdjustID call set =$adId"
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }


        suspend fun initFirebase(context: Context) {
            coroutineScope {
                launchWithSupervisorJob(Dispatchers.Default) {
                    kotlin.runCatching {
                        mUserUid = UUID.randomUUID().toString()
                    }
                    SdkF.sodaC(context)
                }
                launchWithSupervisorJob(Dispatchers.Default) {
                    runCatching {
                        val versionCode = IKSdkUtilsCore.getVersionApp(context)
                        if (versionCode > 0) {
                            Firebase.messaging.subscribeToTopic(
                                IkmCoreFMService.IKN_SDK_TOPIC + "_" + versionCode
                            )
                        }
                    }
                }
                launchWithSupervisorJob(Dispatchers.Default) {
//                    IKTrackingHelper.setUserProperty(
//                        context,
//                        IKTrackingConst.ParamName.SDK_VERSION,
//                        BuildConfig.versionCodeSdk.toString()
//                    )
//                    IKTrackingHelper.setUserProperty(
//                        context,
//                        IKTrackingConst.ParamName.SDK_DATA_SOURCE_VERSION,
//                        IKDataSourceHelper.getSdkVersionCode().toString()
//                    )
//                    CoreTracking.customizeTracking(
//                        IKTrackingConst.EventName.SDK_VERSION_TRACK, false,
//                        IKTrackingConst.ParamName.SDK_VERSION_NAME to BuildConfig.versionNameSdk,
//                        IKTrackingConst.ParamName.SDK_DATA_SOURCE_VERSION_NAME to IKDataSourceHelper.getArtifactIdSdk(),
//                    )
                }
                launchWithSupervisorJob(Dispatchers.Default) {
                    val ir = IKSdkDataStore.getBoolean(IKSdkDataStoreConst.SODA_CC, false)
                    if (ir) {
                        runCatching {
                            Firebase.messaging.subscribeToTopic(IkmCoreFMService.IKN_SDK_TOPIC)
                            Firebase.messaging.subscribeToTopic(IkmCoreFMService.IKN_SDK_TOPIC + "_wlIkm")
                        }
                        runCatching {
                            Firebase.messaging.unsubscribeFromTopic(
                                IkmCoreFMService.IKN_SDK_TOPIC + "_" + "ik_ukn"
                            )
                        }
                        runCatching {
                            val currentLocale: String = runCatching {
                                IKSdkUtilsCore.getCountryCode(context).lowercase()
                            }.getOrNull() ?: "ik_ukn"
                            if (currentLocale.isNotBlank()) {
                                Firebase.messaging.unsubscribeFromTopic(
                                    IkmCoreFMService.IKN_SDK_TOPIC + "_" + currentLocale
                                )
                            } else {

                                Firebase.messaging.unsubscribeFromTopic(
                                    IkmCoreFMService.IKN_SDK_TOPIC + "_" + "ik_ukn"
                                )
                            }
                        }
                    } else {
                        var isIkUnk = false
                        runCatching {
                            val currentLocale: String = runCatching {
                                IKSdkUtilsCore.getCountryCode(context).lowercase()
                            }.getOrNull() ?: "ik_ukn"

                            if (IKSdkUtilsCore.verifyCountry(currentLocale)) {
                                isIkUnk = true
                                Firebase.messaging.subscribeToTopic(
                                    IkmCoreFMService.IKN_SDK_TOPIC + "_" + "ik_ukn"
                                )
                            } else {
                                if (currentLocale.isNotBlank()) {
                                    Firebase.messaging.subscribeToTopic(
                                        IkmCoreFMService.IKN_SDK_TOPIC + "_" + currentLocale
                                    )
                                } else {
                                    isIkUnk = true
                                    Firebase.messaging.subscribeToTopic(
                                        IkmCoreFMService.IKN_SDK_TOPIC + "_" + "ik_ukn"
                                    )
                                }
                            }
                        }
                        runCatching {
                            Firebase.messaging.unsubscribeFromTopic(IkmCoreFMService.IKN_SDK_TOPIC + "_wlIkm")
                        }
                        if (!isIkUnk) {
                            runCatching {
                                Firebase.messaging.subscribeToTopic(IkmCoreFMService.IKN_SDK_TOPIC)
                            }
                        }
                    }
                }

                runCatching {
                    val isDebuggable =
                        0 != (context.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE)
                            ?: false)

//                    if (isDebuggable && IKDataCoreManager.isEnableDebug())
//                        FirebaseMessaging.getInstance().token.addOnCompleteListener {
//                            runCatching {
//                                showLogSdk("ftoken") { it.result }
//                            }
//                        }
                }
            }
        }

        suspend fun initAdsApplicationSdk(
            application: Application
        ) {
            coroutineScope {
                // Task khởi tạo cấu hình SDK
                launchWithSupervisorJob(Dispatchers.IO) {
                    kotlin.runCatching {
                        IKSdkController.initConfig(application.applicationContext)
                        IKSdkController.initAdsConfig(application.applicationContext)
                    }.onFailure { e ->
                        showLogSdk("SDK Initialization") { "Init SDKConfig failed: ${e.message}" }
                    }
                }

                // Task khởi tạo SDKDataHolder
                launchWithSupervisorJob(Dispatchers.IO) {
                    kotlin.runCatching {
                        SDKDataHolder.initLib()
                        SDKDataHolder.FFun.dffC()
                    }.onFailure { e ->
                        showLogSdk("SDKDataHolder Initialization") { "Init SDKDataHolder failed: ${e.message}" }
                    }
                }

                // Task khởi tạo MobileAds
                launchWithSupervisorJob(Dispatchers.IO) {
                    kotlin.runCatching {
                        MobileAds.initialize(application.applicationContext) {}
                    }.onFailure { e ->
                        showLogSdk("MobileAds Initialization") { "Init MobileAds failed: ${e.message}" }
                    }
                }

                // Task khởi tạo IKameAdController
                launchWithSupervisorJob(Dispatchers.IO) {
//                    kotlin.runCatching {
//                        IKameAdController.initialize(application)
//                    }.onFailure { e ->
//                        showLogSdk("IKameAdController Initialization") { "Init IKameAdController failed: ${e.message}" }
//                    }
                }

                // Task khởi tạo IKApplovinHelper
                launchWithSupervisorJob(Dispatchers.IO) {
                    kotlin.runCatching {
                        IKApplovinHelper.initialize(application.applicationContext)
                    }.onFailure { e ->
                        showLogSdk("IKApplovinHelper Initialization") { "Init IKApplovinHelper failed: ${e.message}" }
                    }
                }

                // Task khởi tạo Adjust
                launchWithSupervisorJob(Dispatchers.IO) {
                    kotlin.runCatching {
                        initAdjust(application.applicationContext)
                    }.onFailure { e ->
                        showLogSdk("Adjust Initialization") { "Init Adjust failed: ${e.message}" }
                    }
                }
                // Task khởi tạo liên quan đến giao diện và vòng đời ứng dụng
                launchWithSupervisorJob(Dispatchers.Main) {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(
                        SDKLifecycleObserver(sdkLifecycleCallback())
                    )
                    // Activity Lifecycle Callback
                    application.registerActivityLifecycleCallbacks(
                        activityLifecycleCallbacks()
                    )
                }
            }
        }

        private fun activityLifecycleCallbacks() =
            object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    addActRe(activity)
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivityCreated(activity, savedInstanceState)
                        }
                    }
                    HandleEna.sendTrackOpenApp(activity.intent?.extras)
                }

                override fun onActivityStarted(activity: Activity) {
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivityStarted(activity)
                        }
                    }
                }

                override fun onActivityResumed(activity: Activity) {
                    Adjust.onResume()
                    addActRe(activity)
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivityResumed(activity)
                        }
                    }
                }

                override fun onActivityPaused(activity: Activity) {
                    Adjust.onPause()
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivityPaused(activity)
                        }
                    }
                    addActRe(activity)
                }

                override fun onActivityStopped(activity: Activity) {
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivityStopped(activity)
                        }
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivitySaveInstanceState(activity, outState)
                        }
                    }
                }

                override fun onActivityDestroyed(activity: Activity) {
                    activityScope.launchWithSupervisorJob {
                        kotlin.runCatching {
                            if (listActivity.containsKey(activity::class.java.name))
                                listActivity.remove(activity::class.java.name)
                        }
                    }
                    kotlin.runCatching {
                        mListActLifecycleCallbacks.forEach {
                            it.onActivityDestroyed(activity)
                        }
                    }
                }

            }

        private fun addActRe(activity: Activity) {
            activityScope.launchWithSupervisorJob {
                kotlin.runCatching {
                    if (!listActivity.containsKey(activity::class.java.name))
                        listActivity[activity::class.java.name] = activity

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        listActivity.entries.removeIf {
                            it.value == null || it.value?.isDestroyed == true || it.value?.isFinishing == true
                        }
                    } else {
                        if (!listActivity.containsKey(activity::class.java.name))
                            listActivity[activity::class.java.name] = activity
                        val xx = listActivity.filter {
                            it.value != null && it.value?.isDestroyed == false && it.value?.isFinishing == false
                        }
                        listActivity.clear()
                        listActivity.putAll(xx)
                    }
                }
            }
        }

        private fun sdkLifecycleCallback() =
            object : SDKLifecycleCallback {
                override fun onStart(owner: LifecycleOwner?) {
                    isAppLive = true
                    owner?.lifecycleScope?.launchWithSupervisorJob {
                        onAppForeground()
                    }
                    owner?.lifecycleScope?.launchWithSupervisorJob {
                        runCatching {
                            if (IKUtils.isChineseDevice() && listActivity.isEmpty()) {
//                                IKAppOpenController.clearAdCache()
//                                IKInterController.clearAdCache()
//                                IKBannerInlineController.clearAdCache()
//                                IKRewardedController.clearAdCache()
                            }
                        }
                    }
                }

                override fun onPause(owner: LifecycleOwner?) {
                }

                override fun onCreate(owner: LifecycleOwner?) {
                }

                override fun onResume(owner: LifecycleOwner?) {
                    isAppLive = true
                }

                override fun onStop(owner: LifecycleOwner?) {
                    isAppLive = false
                    HandleEna.callCallback()
                }

                override fun onDestroy(owner: LifecycleOwner?) {
                    HandleEna.setHandleEnable(false)
                }
            }

        suspend fun onAppForeground() {
            showLogD("onAppForeground") { "start run" }
            if (!IKSdkOptions.mEnableShowResumeAds) {
                showLogD("onAppForeground") { "enableShowAds=${IKSdkOptions.mEnableShowResumeAds}" }
                return
            }
            if (SdkF.onHandleFirstAd) {
                showLogD("onAppForeground") { "onHandleFirstAd running1" }
                return
            }
            showLogD("onAppForeground") { "start check" }
            if (IKSdkOptions.delayHandlerShowResumeAds > 200)
                delay(IKSdkOptions.delayHandlerShowResumeAds)
            val foundActivity = withContext(Dispatchers.Default) {
                if (containAdsActivity())
                    delay(200)
                if (containAdsActivity())
                    delay(200)
                kotlin.runCatching {
                    filterActivityElements { it.value != null }?.values?.lastOrNull()
                }.getOrNull()
            }
            if (SdkF.onHandleFirstAd) {
                showLogD("onAppForeground") { "onHandleFirstAd running2" }
                return
            }
            if (foundActivity != null) {
                val validActivity = withContext(Dispatchers.Default) {
                    mActivityEnableShowResumeAd.find { it == foundActivity.javaClass } == null
                }
                if (validActivity) {
                    showLogD("onAppForeground") { "activity show resume ads not valid:$foundActivity" }
                    return
                }
                showResumeOpenAd(foundActivity)
            } else {
                showLogD("sdkLifecycle") { "no activity found" }
            }
        }
    }

    object SdkF {
        private var mHasInitSDK: Boolean = false
        var mContainAdActivity = arrayListOf<String>()
        private val mJob = SupervisorJob()
        private val mSDKFUiScope = CoroutineScope(Dispatchers.Main + mJob)
        var myService: IKSdkConnectInterface? = null
        private val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                kotlin.runCatching {
                    myService = IKSdkConnectInterface.Stub.asInterface(service)
                    checkLogLevel(myService?.ikLogLevel())
                    myService?.registerCallback(object : IKSdkConnectDataCallback.Stub() {
                        override fun onDataReceived(data: String?) {
                            checkLogLevel(data)
                        }
                    })
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
//                myService = null
                checkLogLevel(null)
            }
        }

        fun checkLogLevel(level: String?) {
            IKDataCoreManager.ikDmLv1 = level == IKSdkDefConst.Logs.Level.LEVEL_1
            IKDataCoreManager.ikDmLvSdk = level == IKSdkDefConst.Logs.Level.LEVEL_SDK
            IKDataCoreManager.ikDmLv = level ?: ""
//            IKLogs.dNone("bdm") { "bdm value=${IKDataCoreManager.isEnableDebug()}" }
        }

        var onHandleFirstAd = false
            set(value) {
                if (value) {
                    mSDKFUiScope.launchWithSupervisorJob {
                        runCatching {
                            mJob.cancel()
                        }
                        delay(30000)

                        field = false
                    }
                }
                field = value
            }

        suspend fun sodaC(context: Context) {
            withContext(Dispatchers.IO) {
                val isAppInt = runCatching {
//                    IKSdkConnectInterface::class.java.`package`?.let {
//                        IKSdkUtilsCore.isPackageInstalled(
//                            it.name,
//                            context.packageManager
//                        )
//                    }
                }.getOrNull() ?: false
//                if (!isAppInt) {
//                    return@withContext
//                }
                runCatching {
                    val intent = Intent("com.ikame.android.sdk.ik_log.IKSdkConnectInterface")
                    intent.setPackage("com.ikame.android.sdk.ik_log")
                    context.bindService(
                        intent, connection,
                        ComponentActivity.BIND_AUTO_CREATE
                    )
                }
            }
        }

        suspend fun isLoadingNCL(): Boolean {
            return IKSdkDataStore.getBoolean(IKSdkDataStoreConst.ENABLE_LOADING_NCL, false)
        }

        suspend fun isOpenNCL(repo: IKDataRepository?): Boolean {
            return repo?.getConfigNCL(IKSdkDefConst.AdScreen.NATIVE_OPEN_CUSTOM)?.enable == true
        }

        suspend fun isInterNCL(repo: IKDataRepository?): Boolean {
            return repo?.getConfigNCL(IKSdkDefConst.AdScreen.NATIVE_INTER_CUSTOM)?.enable == true
        }
    }

    object HandleEna {
        var endPointAt: Class<*>? = null
        private val mHandler: Handler? by lazy {
            Handler(Looper.getMainLooper())
        }
        private var mRunner = Runnable {
            onHandleEnable = false
            onHandleFx = false
        }
        private var mDelayTimeLoadingAds = 60 * 60_000L
        var onHandleEnable = false
            private set(value) {
                if (!value) {
                    mHandler?.removeCallbacks(mRunner)
                }
                field = value
                onHandleFx = field
            }
        var onHandleFx = false
        fun callCallback() {
            mHandler?.removeCallbacks(mRunner)
            mHandler?.postDelayed(mRunner, mDelayTimeLoadingAds)
        }

        fun setHandleEnable(value: Boolean) {
            onHandleEnable = value
            onHandleFx = value
        }

        private var lastTimeSendTrack = 0L
        fun sendTrackOpenApp(bundle: Bundle?) {
            if (bundle?.getString(IkmCoreFMService.NFX_KX)
                == IkmCoreFMService.NFX_VX
            ) {
                setHandleEnable(true)
                if (System.currentTimeMillis() - lastTimeSendTrack < 60000) {
                    return
                }
                lastTimeSendTrack = System.currentTimeMillis()
//                IKSdkTrackingHelper.customizeTracking(
//                    IkmCoreFMService.IKN_TRACKING_TRACK,
//                    Pair("act", "op_a")
//                )
//                if (bundle.getString(IkmCoreFMService.KEY_IKM_ACTION) == "yes")
//                    IKSdkTrackingHelper.customizeTracking(
//                        IkmCoreFMService.IKN_TRACKING_TRACK,
//                        Pair("act", "click_nf")
//                    )
            }
        }

        @SuppressLint("DiscouragedApi")
        fun getSmallIcon(
            params: NotificationParams,
            appContext: Context
        ): Int {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                return R.drawable.baseline_notifications_24
            }
            return runCatching {
                val resources: Resources = appContext.resources
                val pkgName: String = appContext.packageName
                val resourceKey = params.getString(Constants.MessageNotificationKeys.ICON)

                if (!resourceKey.isNullOrBlank()) {
                    // if the message contains a specific icon name, try to find it in the resources.
                    var iconId = resources.getIdentifier(resourceKey, "drawable", pkgName)
                    if (iconId != 0 && isValidIcon(resources, iconId)) {
                        return iconId
                    }

                    // Also try the mipmap resources if not found in drawable
                    iconId = resources.getIdentifier(resourceKey, "mipmap", pkgName)
                    if (iconId != 0 && isValidIcon(resources, iconId)) {
                        return iconId
                    }
                }
                val appInfo =
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            appContext.packageManager.getApplicationInfo(
                                appContext.packageName,
                                PackageManager.ApplicationInfoFlags.of(0)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            appContext.packageManager.getApplicationInfo(
                                appContext.packageName,
                                PackageManager.GET_META_DATA
                            )
                        }
                    }.getOrNull()
                val manifestMetadata = getManifestMetadata(
                    appInfo
                )
                var iconId =
                    manifestMetadata?.getInt(CommonNotificationBuilder.METADATA_DEFAULT_ICON, 0)
                        ?: 0
                if (iconId == 0 || !isValidIcon(resources, iconId)) {
                    iconId = appInfo?.icon ?: 0
                }
                if (iconId == 0 || !isValidIcon(resources, iconId)) {
                    // Wow, app doesn't have a launcher icon. Falling back on icon-placeholder used by the OS.
                    iconId = R.drawable.baseline_notifications_24
                }
                return iconId
            }.getOrNull() ?: R.drawable.baseline_notifications_24
        }


        private fun getManifestMetadata(info: ApplicationInfo?): Bundle? {
            if (info?.metaData != null) {
                return info.metaData
            }
            return Bundle.EMPTY
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun isValidIcon(resources: Resources, resId: Int): Boolean {
            // if the fix (ag/2468399) is ever backported to API 26, take SECURITY_PATCH into account.
            return if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                true
            } else try {
                val icon = resources.getDrawable(resId,  /* theme= */null)
                icon !is AdaptiveIconDrawable
            } catch (ex: Resources.NotFoundException) {
                false
            }
        }
    }

    private fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk("handleFunc") {
            "${tag}:" + message.invoke()
        }
    }

    private fun showLogD(tag: String, message: () -> String) {
        IKLogs.d("handleFunc") {
            "${tag}:" + message.invoke()
        }
    }
}