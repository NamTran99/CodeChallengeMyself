/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ads.activity.billing.core

import android.text.TextUtils
import android.util.Base64
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.IKDataCoreManager
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.pub.IKProductType
import com.example.ads.activity.data.dto.pub.SdkIapPackageDto
import com.example.ads.activity.data.dto.pub.SdkTempIapPackageDto
import com.example.ads.activity.data.local.IKSdkDataStoreBilling
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.listener.keep.SDKIAPProductIDProvider
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkUtilsCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
internal object IkBillingSecurity {
    private const val TAG = "IABUtil/Security"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"
    private var isRemoveAds: Pair<Long, Boolean> =
        Pair(0, false)
    private const val TIME_CACHE_VALID = 180_000
    private var cacheIapPackage: Pair<Long, ArrayList<SdkIapPackageDto>> = Pair(0, arrayListOf())
    private const val TIME_CACHE_VALID_CACHE_PURCHASE_HISTORY = 60_000
    private var tempCachePurchaseHistory: SdkTempIapPackageDto? = null
    private val gson = Gson()
    var provider: SDKIAPProductIDProvider? = null

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key. The data also contains the [PurchaseState]
     * and product ID of the purchase.
     *
     * @param productId       the product Id used for debug validation.
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData      the signed JSON string (signed, not encrypted)
     * @param signature       the signature for the data, signed with the private key
     */
    fun verifyPurchase(
        productId: String, base64PublicKey: String?,
        signedData: String, signature: String?
    ): Boolean {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey) ||
            TextUtils.isEmpty(signature)
        ) {
            return productId == "android.test.purchased" || productId == "android.test.canceled" ||
                    productId == "android.test.refunded" || productId == "android.test.item_unavailable"
        }
        val key = generatePublicKey(base64PublicKey)
        return verify(key, signedData, signature)
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    fun generatePublicKey(encodedPublicKey: String?): PublicKey {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            throw IllegalArgumentException(e)
        } catch (e: IllegalArgumentException) {
            throw e
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey  public key associated with the developer account
     * @param signedData signed data from server
     * @param signature  server signature
     * @return true if the data and signature match
     */
    fun verify(publicKey: PublicKey?, signedData: String, signature: String?): Boolean {
        val sig: Signature
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray())
            return sig.verify(Base64.decode(signature, Base64.DEFAULT))
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: InvalidKeyException) {
        } catch (e: SignatureException) {
        } catch (e: IllegalArgumentException) {
        }
        return false
    }

    suspend fun isRemoveShowAds(): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val current = System.currentTimeMillis()
                if ((current - isRemoveAds.first) > TIME_CACHE_VALID) {
                    val removeList =
                        provider
                            ?.listProductIDsRemoveAd()
                    val removeConfig =
                        IKSdkUtilsCore.parseSubObject(IKSdkDefConst.Config.CONFIG_OTHER_WILL_REMOVE_ADS)
                    val listProductID = arrayListOf<String>()
                    runCatching {
                        if (removeList != null) {
                            listProductID.addAll(removeList)
                        }
                    }.onFailure {
                        delay(200)
                        runCatching {
                            if (removeList != null) {
                                listProductID.addAll(removeList)
                            }
                        }
                    }
                    kotlin.runCatching {
                        listProductID.addAll(removeConfig)
                    }.onFailure {
                        delay(200)
                        runCatching {
                            listProductID.addAll(removeConfig)
                        }
                    }
                    isRemoveAds = Pair(current, getIapPackage().find {
                        listProductID.contains(it.productId)
                    } != null)
                }
            }

            return@withContext isRemoveAds.second
        }
    }

    suspend fun setTempCacheIapPackage(productId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val listConfigPur = ArrayList<String>()
                val listConfigSub = ArrayList<String>()
                var pack: SdkTempIapPackageDto? = null
                listConfigSub.addAll(
                    provider?.listProductIDsSubscription()
                        ?: listOf()
                )
                kotlin.runCatching {
                    listConfigSub.addAll(
                        IKSdkUtilsCore.parseSubObject(
                            IKSdkDefConst.CONFIG_OTHER_SUB
                        )
                    )
                    kotlin.runCatching {
                        val listConfigSubData =
                            IKDataCoreManager.otherConfig[IKSdkDefConst.Config.OTHER_SUB_ID]?.toString()
                                ?: IKSdkDefConst.EMPTY
                        Gson().fromJson<List<String>>(
                            listConfigSubData,
                            List::class.java
                        )?.also {
                            listConfigSub.addAll(it)
                        }
                    }
                    val listSub = listConfigSub.distinct()
                    if (listSub.contains(productId))
                        pack = SdkTempIapPackageDto(
                            productId,
                            IKProductType.SUBS.value,
                            System.currentTimeMillis()
                        )
                }
                if (pack == null) {
                    listConfigPur.addAll(
                        provider?.listProductIDsPurchase()
                            ?: arrayListOf()
                    )
                    kotlin.runCatching {
                        listConfigPur.addAll(
                            IKSdkUtilsCore.parseSubObject(
                                IKSdkDefConst.CONFIG_OTHER_IN_APP
                            )
                        )
                        kotlin.runCatching {
                            val listConfigPurData =
                                IKDataCoreManager.otherConfig[IKSdkDefConst.Config.OTHER_PUR_ID]?.toString()
                                    ?: IKSdkDefConst.EMPTY
                            Gson().fromJson<List<String>>(
                                listConfigPurData,
                                List::class.java
                            )?.also {
                                listConfigPur.addAll(it)
                            }
                        }
                        val listPur = listConfigPur.distinct()
                        if (listPur.contains(productId))
                            pack = SdkTempIapPackageDto(
                                productId,
                                IKProductType.INAPP.value,
                                System.currentTimeMillis()
                            )
                    }
                }
                if (pack == null)
                    return@runCatching
                tempCachePurchaseHistory = pack
                //reset to can check isRemoveShowAds
                isRemoveAds = Pair(0, false)
                IKSdkDataStoreBilling.putString(
                    IKSdkDataStoreConst.Billing.IK_TEMP_CACHE_PURCHASE_HISTORY,
                    gson.toJson(tempCachePurchaseHistory)
                )
            }
        }

    }

    suspend fun setIapPackage(dtos: List<SdkIapPackageDto>?) {
        isRemoveAds = Pair(0, false)
        if (dtos.isNullOrEmpty())
            return
        kotlin.runCatching {

            var list: ArrayList<SdkIapPackageDto> = arrayListOf()
            list = kotlin.runCatching {
                val cacheDto: ArrayList<SdkIapPackageDto> = gson.fromJson(
                    IKSdkDataStoreBilling.getString(
                        IKSdkDataStoreConst.Billing.KEY_IAP_PACKAGE,
                        IKSdkDefConst.EMPTY
                    ),
                    object : TypeToken<ArrayList<SdkIapPackageDto>>() {}.type
                )
                cacheDto
            }.getOrNull() ?: arrayListOf()
            val listAdd: ArrayList<SdkIapPackageDto> = arrayListOf()
            kotlin.runCatching {
                dtos.forEach {
                    if (list.isNullOrEmpty() || !list.contains(it))
                        listAdd.add(it)
                }
                if (listAdd.isNotEmpty()) {
                    listAdd.addAll(list)
                    IKSdkDataStoreBilling.putString(
                        IKSdkDataStoreConst.Billing.KEY_IAP_PACKAGE,
                        gson.toJson(listAdd)
                    )
                    cacheIapPackage = Pair(System.currentTimeMillis(), listAdd)
                }
            }
            if (listAdd.isEmpty()) {
                return
            }
            kotlin.runCatching {
                val isRemoveAds = list.find {
                    provider?.listProductIDsRemoveAd()
                        ?.apply {
                            addAll(IKSdkUtilsCore.parseSubObject(IKSdkDefConst.Config.CONFIG_OTHER_WILL_REMOVE_ADS))
                        }?.contains(it.productId) == true
                } != null
                val userPropertyType = if (isRemoveAds)
                    IKSdkDefConst.UserPropertyType.REMOVE_ADS
                else {
                    if (list.isNotEmpty())
                        IKSdkDefConst.UserPropertyType.PREMIUM
                    else IKSdkDefConst.UserPropertyType.NORMAL
                }
//                IKSdkApplicationProvider.getContext()?.let {
//                    FirebaseAnalytics.getInstance(it)
//                        .setUserProperty(IKSdkDefConst.USER_PROPERTY_TYPE, userPropertyType)
//                }
            }
        }
    }

    suspend fun getIapPackage(): ArrayList<SdkIapPackageDto> {
        return withContext(Dispatchers.Default) {

            if (System.currentTimeMillis() - cacheIapPackage.first > TIME_CACHE_VALID &&
                cacheIapPackage.second.isNotEmpty()
            ) {
                return@withContext cacheIapPackage.second
            }
            var listResult: ArrayList<SdkIapPackageDto> = cacheIapPackage.second

            runCatching l@{
                if (cacheIapPackage.second.isEmpty()) {
                    kotlin.runCatching {
                        val data = IKSdkDataStoreBilling.getString(
                            IKSdkDataStoreConst.Billing.KEY_IAP_PACKAGE,
                            IKSdkDefConst.EMPTY
                        )
                        if (data.isBlank()) {
                            cacheIapPackage = Pair(0, arrayListOf())
                            return@l
                        }
                        val gson = Gson()
                        val list = kotlin.runCatching {
                            gson.fromJson<ArrayList<SdkIapPackageDto>>(
                                data,
                                object : TypeToken<ArrayList<SdkIapPackageDto>>() {}.type
                            )
                        }.getOrNull() ?: arrayListOf()
                        if (list.isEmpty()) {
                            cacheIapPackage = Pair(0, arrayListOf())
                            return@l
                        }
                        listResult = list
                    }
                    cacheIapPackage = Pair(System.currentTimeMillis(), listResult)
                } else {
                    launch {
                        kotlin.runCatching {
                            val data = IKSdkDataStoreBilling.getString(
                                IKSdkDataStoreConst.Billing.KEY_IAP_PACKAGE,
                                IKSdkDefConst.EMPTY
                            )
                            if (data.isBlank()) {
                                cacheIapPackage = Pair(0, arrayListOf())
                                return@launch
                            }
                            val gson = Gson()
                            val list = kotlin.runCatching {
                                gson.fromJson<ArrayList<SdkIapPackageDto>>(
                                    data,
                                    object : TypeToken<ArrayList<SdkIapPackageDto>>() {}.type
                                )
                            }.getOrNull() ?: arrayListOf()
                            if (list.isEmpty()) {
                                cacheIapPackage = Pair(0, arrayListOf())
                                return@launch
                            }
                            cacheIapPackage = Pair(System.currentTimeMillis(), list)
                        }
                    }
                }
                if (listResult.isEmpty()) {
                    if (tempCachePurchaseHistory == null)
                        kotlin.runCatching {
                            tempCachePurchaseHistory = gson.fromJson(
                                IKSdkDataStoreBilling.getString(
                                    IKSdkDataStoreConst.Billing.IK_TEMP_CACHE_PURCHASE_HISTORY,
                                    ""
                                ), SdkTempIapPackageDto::class.java
                            )
                        }
                    if (tempCachePurchaseHistory != null) {
                        runCatching {
                            if (System.currentTimeMillis() - tempCachePurchaseHistory!!.time > TIME_CACHE_VALID_CACHE_PURCHASE_HISTORY
                            ) {
                                listResult.add(
                                    SdkIapPackageDto(
                                        tempCachePurchaseHistory!!.productId,
                                        tempCachePurchaseHistory!!.productType
                                    )
                                )
                            }
                        }
                    }
                }
            }

            return@withContext listResult
        }
    }

    suspend fun clearIapPackage() {
        cacheIapPackage = Pair(0, arrayListOf())
        isRemoveAds = Pair(0, false)
        kotlin.runCatching {
            IKSdkDataStoreBilling.remove(IKSdkDataStoreConst.Billing.KEY_IAP_PACKAGE)
        }
        kotlin.runCatching {
            IKSdkDataStoreBilling.remove(IKSdkDataStoreConst.Billing.IK_TEMP_CACHE_PURCHASE_HISTORY)
        }
        kotlin.runCatching {
//            IKSdkApplicationProvider.getContext()?.let {
//                FirebaseAnalytics.getInstance(it)
//                    .setUserProperty(
//                        IKSdkDefConst.USER_PROPERTY_TYPE,
//                        IKSdkDefConst.UserPropertyType.NORMAL
//                    )
//            }
        }
    }
}