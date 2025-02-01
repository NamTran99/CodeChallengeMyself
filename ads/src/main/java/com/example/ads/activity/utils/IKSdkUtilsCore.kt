package com.example.ads.activity.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.StrictMode
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT
import android.telephony.TelephonyManager.NETWORK_TYPE_CDMA
import android.telephony.TelephonyManager.NETWORK_TYPE_EDGE
import android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B
import android.telephony.TelephonyManager.NETWORK_TYPE_GPRS
import android.telephony.TelephonyManager.NETWORK_TYPE_GSM
import android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP
import android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA
import android.telephony.TelephonyManager.NETWORK_TYPE_IDEN
import android.telephony.TelephonyManager.NETWORK_TYPE_IWLAN
import android.telephony.TelephonyManager.NETWORK_TYPE_LTE
import android.telephony.TelephonyManager.NETWORK_TYPE_NR
import android.telephony.TelephonyManager.NETWORK_TYPE_TD_SCDMA
import android.telephony.TelephonyManager.NETWORK_TYPE_UMTS
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children
import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.example.ads.activity.IKSdkController
import com.example.ads.activity.IKSdkOptions
import com.example.ads.R
import com.example.ads.activity.billing.core.IkBillingSecurity
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.core.SDKDataHolder
////import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.db.IkmSdkCacheFunc
import com.example.ads.activity.data.dto.pub.SDKNetworkType
import com.example.ads.activity.data.dto.pub.SdkIapPackageDto
//import com.example.ads.activity.format.intertial.IKInterController
//import com.example.ads.activity.format.open_ads.IKAppOpenController
//import com.example.ads.activity.format.rewarded.IKRewardedController
import com.example.ads.activity.utils.IKSdkDefConst.INTER_SPLASH_COUNTRY_CODE
import com.example.ads.activity.utils.IKSdkDefConst.cmpCountryCodeList
import com.example.ads.activity.utils.IkmSdkCoreFunc.SdkF.mContainAdActivity
import com.example.ads.activity.widgets.IkmWidgetAdCollapseLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object IKSdkUtilsCore {

    fun openBrowser(context: Context?, url: String?) {
        if (context == null)
            return
        try {
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {

        }
    }

    fun openStore(context: Context?, packageName: String?) {
        if (context == null)
            return
        try {
            val webpage: Uri =
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun isConnectionAvailable(): Boolean {
        return IkmSdkCoreFunc.AppF?.isInternetAvailable == true
    }

    suspend fun isConnectionAvailableAsync(): Boolean {
        return withContext(Dispatchers.IO) {
            var status = IkmSdkCoreFunc.AppF?.isInternetAvailable == true
            if (status)
                return@withContext true
            launch {
//                IKSdkApplicationProvider.getContext()?.let {
//                    IKSdkOptions.reloadNetworkState(it)
//                }
            }
            status = IkmSdkCoreFunc.AppF?.isInternetAvailable == true
            status
        }
    }

    @SuppressLint("MissingPermission")
    fun isInternetAvailable(context: Context?): SDKNetworkType {
        if (context == null) return SDKNetworkType.NotConnect
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities =
                    connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            val tm =
                                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                            try {
                                return when (tm?.dataNetworkType) {
                                    NETWORK_TYPE_GSM, NETWORK_TYPE_CDMA, NETWORK_TYPE_1xRTT,
                                    NETWORK_TYPE_IDEN, NETWORK_TYPE_GPRS, NETWORK_TYPE_EDGE ->
                                        SDKNetworkType.TypeMobile2G

                                    NETWORK_TYPE_TD_SCDMA, NETWORK_TYPE_UMTS, NETWORK_TYPE_EVDO_0,
                                    NETWORK_TYPE_EVDO_A, NETWORK_TYPE_EVDO_B, NETWORK_TYPE_HSPA,
                                    NETWORK_TYPE_HSDPA, NETWORK_TYPE_HSUPA, NETWORK_TYPE_EHRPD,
                                    NETWORK_TYPE_HSPAP ->
                                        SDKNetworkType.TypeMobile3G

                                    NETWORK_TYPE_IWLAN, NETWORK_TYPE_LTE -> SDKNetworkType.TypeMobile4G
                                    NETWORK_TYPE_NR -> SDKNetworkType.TypeMobile5G
                                    else -> SDKNetworkType.TypeMobileOther
                                }
                            } catch (e: Exception) {
                                return try {
                                    sdkNetworkTypeBelowQ(connectivityManager)
                                } catch (e: Exception) {
                                    SDKNetworkType.TypeMobileAndroidQ
                                }
                            }
                        }

                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return SDKNetworkType.TypeWifi
                        }

                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return SDKNetworkType.TypeEthernet
                        }
                    }
                }
            } else {
                return sdkNetworkTypeBelowQ(connectivityManager)
            }
        } catch (_: Exception) {

        }

        return SDKNetworkType.NotConnect
    }

    @Suppress("DEPRECATION")
    private fun sdkNetworkTypeBelowQ(connectivityManager: ConnectivityManager?): SDKNetworkType {
        val netInfo = connectivityManager?.activeNetworkInfo
        when (netInfo?.type) {
            null -> return SDKNetworkType.NotConnect
            ConnectivityManager.TYPE_WIFI -> {
                return SDKNetworkType.TypeWifi
            }

            ConnectivityManager.TYPE_ETHERNET -> {
                return SDKNetworkType.TypeEthernet
            }

            ConnectivityManager.TYPE_MOBILE -> {
                return when (netInfo.subtype) {
                    NETWORK_TYPE_GSM, NETWORK_TYPE_CDMA, NETWORK_TYPE_1xRTT,
                    NETWORK_TYPE_IDEN, NETWORK_TYPE_GPRS, NETWORK_TYPE_EDGE ->
                        SDKNetworkType.TypeMobile2G

                    NETWORK_TYPE_TD_SCDMA, NETWORK_TYPE_UMTS, NETWORK_TYPE_EVDO_0,
                    NETWORK_TYPE_EVDO_A, NETWORK_TYPE_EVDO_B, NETWORK_TYPE_HSPA,
                    NETWORK_TYPE_HSDPA, NETWORK_TYPE_HSUPA, NETWORK_TYPE_EHRPD,
                    NETWORK_TYPE_HSPAP ->
                        SDKNetworkType.TypeMobile3G

                    NETWORK_TYPE_IWLAN, NETWORK_TYPE_LTE -> SDKNetworkType.TypeMobile4G
                    NETWORK_TYPE_NR -> SDKNetworkType.TypeMobile5G
                    else -> SDKNetworkType.TypeMobileOther
                }
            }

            else -> {
                return SDKNetworkType.TypeOther
            }
        }
    }

    @Deprecated(
        message = "This function may block the UI thread. Use canLoadAdAsync instead.",
        replaceWith = ReplaceWith("canLoadAdAsync()"),
        level = DeprecationLevel.WARNING
    )
    fun canLoadAd(): Boolean {
        return runBlocking(Dispatchers.Default) {
            return@runBlocking isConnectionAvailableAsync() &&
                    !IkBillingSecurity.isRemoveShowAds()
        }
    }

    suspend fun canLoadAdAsync(): Boolean {
        return isConnectionAvailableAsync() &&
                !IkBillingSecurity.isRemoveShowAds()
    }

    fun canShowAd(): Boolean {
        return runBlocking(Dispatchers.Default) {
            return@runBlocking !IkBillingSecurity.isRemoveShowAds()
        }
    }

    suspend fun canShowAdAsync(): Boolean {
        return !IkBillingSecurity.isRemoveShowAds()
    }

    fun decodeJson(base64: String): String {
        return try {
            val data: ByteArray = Base64.decode(base64, Base64.DEFAULT)
            String(data, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            IKSdkDefConst.EMPTY
        }
    }

    fun stringToDate(date: String): Date {
        return try {
            val format = SimpleDateFormat(IKSdkDefConst.FORMAT_DATE_SERVER, Locale.getDefault())
            format.parse(date) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun Float.dpToPx(context: Context): Int {
        return try {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this,
                context.resources.displayMetrics
            ).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun Int.dpToPx(context: Context): Int {
        return try {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun isNetworkFast(context: Context?): Boolean {
        return try {
            val cm =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val nc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm?.getNetworkCapabilities(cm.activeNetwork)
            } else {
                null
            }
            var downSpeed = 0
            kotlin.runCatching {
                downSpeed = nc?.linkDownstreamBandwidthKbps?.div(1000) ?: 0
            }
            downSpeed >= 5
        } catch (e: Exception) {
            true
        }
    }


    fun getMemoryDetail(context: Context?): ActivityManager.MemoryInfo? {
        return try {
            val actManager: ActivityManager? =
                context?.getSystemService(MultiDexApplication.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            context?.let {
                FirebaseAnalytics.getInstance(it)
                    .setUserProperty(IKSdkDefConst.TOTAL_RAM, "${memInfo.totalMem}")
            }
            memInfo
        } catch (e: Exception) {
            null
        }
    }

    fun getUTCTime(): String {
        var time = ""
        kotlin.runCatching {
            val df: DateFormat = SimpleDateFormat(
                "dd/MM/yyyy hh:mm",
                Locale.US
            )
            df.timeZone = TimeZone.getTimeZone("gmt")
            time = df.format(Date())
        }
        return time
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isProductIAPAsync instead.",
        replaceWith = ReplaceWith("isProductIAPAsync()"),
        level = DeprecationLevel.WARNING
    )
    fun isProductIAP(product: SdkIapPackageDto): Boolean {
        return runBlocking(Dispatchers.Default) {
            isProductIAPAsync(product)
        }
    }

    suspend fun isProductIAPAsync(product: SdkIapPackageDto): Boolean {
        return IkBillingSecurity.getIapPackage().contains(product)
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isUserIAPAvailableAsync instead.",
        replaceWith = ReplaceWith("isUserIAPAvailableAsync()"),
        level = DeprecationLevel.WARNING
    )
    fun isUserIAPAvailable(): Boolean {
        return runBlocking(Dispatchers.Default) {
            isUserIAPAvailableAsync()
        }
    }

    suspend fun isUserIAPAvailableAsync(): Boolean {
        return IkBillingSecurity.getIapPackage().isNotEmpty()
    }

    suspend fun getIapPackage(): ArrayList<SdkIapPackageDto> {
        return IkBillingSecurity.getIapPackage()
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isProductIAPAsync instead.",
        replaceWith = ReplaceWith("isProductIAPAsync()"),
        level = DeprecationLevel.WARNING
    )
    fun isProductIAP(product: List<SdkIapPackageDto>): Boolean {
        return runBlocking(Dispatchers.Default) {
            return@runBlocking isProductIAPAsync(product)
        }
    }

    suspend fun isProductIAPAsync(product: List<SdkIapPackageDto>): Boolean {
        return try {
            IkBillingSecurity.getIapPackage().find { product.contains(it) } != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Run provided block only if current process is primary process
     */
    inline fun Application.runIfPrimaryProcess(block: () -> Unit) {
        if (isPrimaryProcess) {
            block()
        }
    }

    /**
     * @return `true` if current process is primary process, `false` otherwise
     */
    inline val Application.isPrimaryProcess: Boolean
        get() = currentProcessFullName == packageName

    /**
     * @return full name of current process
     *
     * Full name looks like `your.app.package:yourProcessName`
     */
    val currentProcessFullName: String?
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Application.getProcessName()
            } else {
                readProcessName(Process.myPid())
            }
        }

    private fun readProcessName(pid: Int): String? {
        if (pid <= 0) {
            return null
        }

        return runCatching {
            val bufferedReader: BufferedReader
            val oldThreadPolicy = StrictMode.allowThreadDiskReads()

            try {
                bufferedReader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            } finally {
                StrictMode.setThreadPolicy(oldThreadPolicy)
            }

            val processName = bufferedReader.readLine().trim { it <= ' ' }

            runCatching { bufferedReader.close() }

            return@runCatching processName
        }.getOrNull()
    }

    suspend fun parseSubObject(key: String): List<String> {
        return listOf()
//        return kotlin.runCatching {
//            val listConfigSubData =
//                IKRemoteDataManager.getRemoteConfigData()[key]?.getString()
//                    ?: IKSdkDefConst.EMPTY
//            Gson().fromJson<List<String>>(
//                listConfigSubData,
//                List::class.java
//            )
//        }.getOrNull() ?: listOf()
    }

    fun minusLoadTime(currentTime: Long): Long {
        return kotlin.runCatching { System.currentTimeMillis() - currentTime }.getOrNull() ?: 0L
    }

    fun getAppPackageInfo(context: Context?): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context?.packageManager?.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION") context?.packageManager?.getPackageInfo(
                context.packageName,
                0
            )
        }
    }

    fun getInstallAppDay(context: Context?): Int {
        var mDayUseApp = 0L
        try {
            val time = getAppPackageInfo(context)?.firstInstallTime ?: System.currentTimeMillis()
            mDayUseApp = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - time)
        } catch (e: Exception) {
//
        }
        return mDayUseApp.toInt()
    }

    suspend fun getDetectedCountry(context: Context): String = coroutineScope {
        val simCountryDeferred = async { detectSIMCountry(context) }
        val networkCountryDeferred = async { detectNetworkCountry(context) }
        val localeCountryDeferred = async { detectLocaleCountry(context) }

        val code = kotlin.runCatching {
            val simCountry = simCountryDeferred.await()?.takeIf { it.isNotBlank() }
            val networkCountry = networkCountryDeferred.await()?.takeIf { it.isNotBlank() }
            val localeCountry = localeCountryDeferred.await()?.takeIf { it.isNotBlank() }

            simCountry ?: networkCountry ?: localeCountry
        }.getOrNull()?.lowercase() ?: "ik_ukn"

        return@coroutineScope code.ifBlank { "ik_ukn" }
    }

    fun verifyCountry(currentLocale: String): Boolean {
        return SDKDataHolder.FFun.dLoc(currentLocale)
    }

    private fun detectSIMCountry(context: Context): String? {
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.simCountryIso
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun detectNetworkCountry(context: Context): String? {
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.networkCountryIso
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun detectLocaleCountry(context: Context): String? {
        try {
            val localeCountryISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0).country
            } else
                @Suppress("DEPRECATION") context.resources.configuration.locale.country
            return localeCountryISO
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getVersionApp(context: Context?): Long {
        return try {
            val pInfo: PackageInfo? = getAppPackageInfo(context)
            val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo?.longVersionCode ?: 0L
            } else {
                @Suppress("DEPRECATION")
                pInfo?.versionCode?.toLong() ?: 0L
            }
            version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun closeOldCollapse() {
        fun viewsFromWM(wmClass: Class<*>, wmInstance: Any): Any? {
            val viewsField = wmClass.getDeclaredField("mViews")
            viewsField.isAccessible = true
            return viewsField.get(wmInstance)
        }
        runCatching {
            @SuppressLint("PrivateApi")
            val wmgClass = Class.forName("android.view.WindowManagerGlobal")
            val wmgInstance = wmgClass.getMethod("getInstance").invoke(null)
            val xx = (wmgInstance?.let { viewsFromWM(wmgClass, it) } as? List<View>)?.filter {
                it::class.java.name.startsWith("android.widget.PopupWindow")
            }?.find { view ->
                ((view as? ViewGroup)?.children?.firstOrNull() as? ViewGroup)?.children?.find {
                    it::class.java.name.startsWith("com.google.android.gms.ads.internal")
                } != null
            }
            (((xx as? ViewGroup)?.children?.firstOrNull() as? ViewGroup)?.children?.find {
                it::class.java.name.startsWith("com.google.android.gms.ads.internal")
            }?.parent as? ViewGroup)?.children?.lastOrNull()?.callOnClick()
        }
        runCatching {
            @SuppressLint("PrivateApi")
            val wmgClass = Class.forName("android.view.WindowManagerGlobal")
            val wmgInstance = wmgClass.getMethod("getInstance").invoke(null)
            val xx = (wmgInstance?.let { viewsFromWM(wmgClass, it) } as? List<View>)?.filter {
                it::class.java.name.startsWith("android.widget.PopupWindow")
            }?.find { view ->
                (view as? ViewGroup)?.children?.firstOrNull() is IkmWidgetAdCollapseLayout
            }
            (xx as? ViewGroup)?.let {
                it.findViewById<View>(R.id.bannerAdCollapseTop_close)?.callOnClick()
                it.findViewById<View>(R.id.bannerAdCollapseBottom_close)?.callOnClick()
            }
        }
    }

    suspend fun getCountryCode(context: Context?): String {
        var countryCode = IkmSdkCacheFunc.Utils.cacheCountryCode
        if (countryCode.isBlank()) {
            countryCode = context?.let { getDetectedCountry(it).lowercase() } ?: ""
            IkmSdkCacheFunc.Utils.cacheCountryCode = countryCode
        }
        return countryCode.ifBlank { "ik_ukn" }
    }

    suspend fun checkCmpCountryCode(context: Context?): Boolean {
        val getCountryCode = getCountryCode(context)

        return kotlin.runCatching {
            cmpCountryCodeList.contains(getCountryCode.lowercase())
        }.getOrNull() ?: false
    }

    suspend fun checkInterSplashCountryCode(context: Context?): Boolean {
        val getCountryCode = getCountryCode(context)
        return withContext(Dispatchers.Default) {
            if (INTER_SPLASH_COUNTRY_CODE.isBlank())
                return@withContext false
            return@withContext kotlin.runCatching {
                val countryCode: List<String> =
                    INTER_SPLASH_COUNTRY_CODE.trim().splitToSequence(",").filter { it.isNotEmpty() }
                        .toList()
                countryCode.contains(getCountryCode)
            }.getOrNull() ?: false
        }
    }

    fun validAdImpressionValue(rev: Double, network: String): Boolean {
        return !(rev == 0.38 && network == "com.google.ads.mediation.pangle.PangleMediationAdapter")
    }

    suspend fun isFullScreenAdShowing(message: (value: String) -> Unit): Boolean {
        return withContext(Dispatchers.Default) {
            val currentActivity = kotlin.runCatching {
                IkmSdkCoreFunc.AppF.filterActivityElements { it.value != null }?.values?.lastOrNull()
            }.getOrNull()

            message.invoke("isFullScreenAdShowing:containAdsActivity name=${currentActivity}")
            if (currentActivity == null)
                return@withContext false
//            val controllerAdShowing =
//                IKInterController.isAdShowing || IKAppOpenController.isAdShowing || IKRewardedController.isAdShowing
//            message.invoke("isFullScreenAdShowing:controllerAdShowing=${controllerAdShowing}")
//            if (controllerAdShowing)
//                return@withContext true

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
            message.invoke("isFullScreenAdShowing:activityFound=${activityFound}")
            activityFound
        }
    }

}