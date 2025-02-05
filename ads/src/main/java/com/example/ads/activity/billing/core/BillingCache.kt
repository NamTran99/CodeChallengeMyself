package com.example.ads.activity.billing.core

import android.content.Context
import android.text.TextUtils
import com.example.ads.activity.billing.dto.PurchaseInfo
import com.example.ads.activity.billing.dto.SdkProductDetails
import com.example.ads.activity.data.local.IKSdkDataStoreBilling
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern


internal class BillingCache(context: Context, key: String) : BillingCacheBase(context) {
    private val data: ConcurrentHashMap<String, PurchaseInfo> = ConcurrentHashMap()
    private val cacheKey: String = key
    private var version: String = ""
    private var uiScope = CoroutineScope(Dispatchers.Main)
    private val mutex = Mutex()

    init {
        load()
    }

    private val preferencesCacheKey: String
        get() = IKSdkDataStoreConst.Billing.createStringRef(preferencesBaseKey + cacheKey)
    private val preferencesVersionKey: String
        get() = IKSdkDataStoreConst.Billing.createStringRef(preferencesBaseKey + cacheKey + VERSION_KEY)
    var productDetailsListCache = ConcurrentHashMap<String, Pair<Long, List<SdkProductDetails>?>>()


    private fun load() {
        uiScope.launchWithSupervisorJob {
            val entries = IKSdkDataStoreBilling.getString(preferencesCacheKey, "")
                .split(Pattern.quote(ENTRY_DELIMITER).toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            mutex.withLock {
                for (entry in entries) {
                    if (!TextUtils.isEmpty(entry)) {
                        val parts = entry.split(Pattern.quote(LINE_DELIMITER).toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        kotlin.runCatching {
                            data[parts[0]] = PurchaseInfo(
                                parts.getOrNull(1),
                                parts.getOrNull(2),
                                parts.getOrNull(3)
                            )
                        }
                    }
                }
                version = currentVersion()
            }
        }
    }

    private fun flush() {
        uiScope.launchWithSupervisorJob {
            withContext(Dispatchers.IO) {
                val output = ArrayList<String?>()
                mutex.withLock {
                    for (productId in data.keys) {
                        val info = data[productId]
                        output.add(
                            productId + LINE_DELIMITER + info?.responseData + LINE_DELIMITER + info?.signature + LINE_DELIMITER + info?.productType
                        )
                    }
                }
                IKSdkDataStoreBilling.putString(
                    preferencesCacheKey,
                    TextUtils.join(ENTRY_DELIMITER, output)
                )
                version = Date().time.toString()
                IKSdkDataStoreBilling.putString(preferencesVersionKey, version)
            }
        }
    }

    suspend fun includesProduct(productId: String?): Boolean {
        return mutex.withLock {
            data.containsKey(productId)
        }
    }

    suspend fun getDetails(productId: String?): PurchaseInfo? {
        return mutex.withLock {
            data[productId]
        }
    }

    fun put(productId: String, purchaseInfo: PurchaseInfo) {
        uiScope.launchWithSupervisorJob {
            mutex.withLock {
                if (!data.containsKey(productId)) {
                    data[productId] = purchaseInfo
                    flush()
                }
            }
        }
    }

    fun remove(productId: String?) {
        uiScope.launchWithSupervisorJob {
            mutex.withLock {
                if (data.containsKey(productId)) {
                    data.remove(productId)
                    flush()
                }
            }
        }
    }

    fun clear() {
        uiScope.launchWithSupervisorJob {
            mutex.withLock {
                data.clear()
                flush()
            }
        }
    }

    private suspend fun currentVersion(): String =
        IKSdkDataStoreBilling.getString(preferencesVersionKey, "0")


    val contents: List<String>
        get() = ArrayList(data.keys)

    override fun toString(): String {
        return TextUtils.join(", ", data.keys)
    }

    companion object {
        private const val ENTRY_DELIMITER = "#####"
        private const val LINE_DELIMITER = ">>>>>"
        private const val VERSION_KEY = ".version"
        private const val TIME_CACHE_VALID = 300_000
        suspend fun Long.verifyCache(
        ): Boolean {
            return withContext(Dispatchers.Default) {
                kotlin.runCatching {
                    val current = System.currentTimeMillis()
                    current - this@verifyCache >= TIME_CACHE_VALID
                }.getOrNull() == true
            }
        }
    }
}
