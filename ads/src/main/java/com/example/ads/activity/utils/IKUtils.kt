package com.example.ads.activity.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.TypedValue
import androidx.annotation.Keep
import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics
import com.example.ads.activity.data.dto.pub.SDKNetworkType
import com.example.ads.activity.data.dto.pub.SdkIapPackageDto
import com.example.ads.activity.data.local.IKSdkDataStoreCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@Keep
object IKUtils {

    @JvmStatic
    fun openBrowser(context: Context?, url: String?) {
        IKSdkUtilsCore.openBrowser(context, url)
    }

    @JvmStatic
    fun openStore(context: Context?, packageName: String?) {
        IKSdkUtilsCore.openStore(context, packageName)
    }

    @JvmStatic
    fun isConnectionAvailable(): Boolean {
        return IKSdkUtilsCore.isConnectionAvailable()

    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getNetworkType(context: Context?): SDKNetworkType {
        return IKSdkUtilsCore.isInternetAvailable(context)
    }


    @Deprecated(
        message = "This function may block the UI thread. Use isRewardAdReadyAsync instead.",
        replaceWith = ReplaceWith("canLoadAdAsync()"),
        level = DeprecationLevel.WARNING
    )
    @JvmStatic
    fun canLoadAd(): Boolean {
        return IKSdkUtilsCore.canLoadAd()
    }


    @JvmStatic
    suspend fun canLoadAdAsync(): Boolean {
        return IKSdkUtilsCore.canLoadAdAsync()
    }

    @JvmStatic
    fun canShowAd(): Boolean {
        return IKSdkUtilsCore.canShowAd()
    }

    @JvmStatic
    suspend fun canShowAdAsync(): Boolean {
        return IKSdkUtilsCore.canShowAd()
    }


    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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


    @JvmStatic
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

    @Deprecated(
        message = "This function may block the UI thread. Use isProductIAPAsync instead.",
        replaceWith = ReplaceWith("isProductIAPAsync()"),
        level = DeprecationLevel.WARNING
    )
    @JvmStatic
    fun isProductIAP(product: SdkIapPackageDto): Boolean {
        return runBlocking(Dispatchers.Default) {
            isProductIAPAsync(product)
        }
    }

    @JvmStatic
    suspend fun isProductIAPAsync(product: SdkIapPackageDto): Boolean {
        return IKSdkUtilsCore.isProductIAPAsync(product)
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isUserIAPAvailableAsync instead.",
        replaceWith = ReplaceWith("isUserIAPAvailableAsync()"),
        level = DeprecationLevel.WARNING
    )
    @JvmStatic
    fun isUserIAPAvailable(): Boolean {
        return IKSdkUtilsCore.isUserIAPAvailable()
    }

    @JvmStatic
    suspend fun isUserIAPAvailableAsync(): Boolean {
        return IKSdkUtilsCore.isUserIAPAvailableAsync()
    }

    @JvmStatic
    suspend fun getIapPackage(): ArrayList<SdkIapPackageDto> {
        return IKSdkUtilsCore.getIapPackage()
    }

    @Deprecated(
        message = "This function may block the UI thread. Use isProductIAPAsync instead.",
        replaceWith = ReplaceWith("isProductIAPAsync()"),
        level = DeprecationLevel.WARNING
    )
    @JvmStatic
    fun isProductIAP(product: List<SdkIapPackageDto>): Boolean {
        return runBlocking(Dispatchers.Default) {
            isProductIAPAsync(product)
        }
    }

    @JvmStatic
    suspend fun isProductIAPAsync(product: List<SdkIapPackageDto>): Boolean {
        return IKSdkUtilsCore.isProductIAPAsync(product)
    }

    @Keep
    @JvmStatic
    fun closeOldCollapse() {
        IKSdkUtilsCore.closeOldCollapse()
    }


    @Keep
    @JvmStatic
    fun getUserId(): String {
        return IKSdkDataStoreCore.getString("user_id", "")
    }

    fun isChineseDevice(): Boolean {
        val chineseManufacturers =
            listOf("Xiaomi", "Huawei", "OnePlus", "Oppo", "Vivo", "Lenovo", "ZTE", "Meizu")
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND

        return chineseManufacturers.any {
            manufacturer.contains(
                it,
                ignoreCase = true
            ) || brand.contains(it, ignoreCase = true)
        }
    }
}