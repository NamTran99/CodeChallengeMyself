package com.example.ads.activity.mediation.custom.utils

import android.os.Bundle
import com.google.android.gms.ads.mediation.MediationAdConfiguration
import com.google.android.gms.ads.mediation.MediationConfiguration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

object IKCustomParamParser {
    suspend fun parseParam(jsonString: String): Map<String, String> {
        return withContext(Dispatchers.Default) {
            runCatching {
                val gson = Gson()
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                return@withContext gson.fromJson(jsonString, mapType)
            }.getOrNull() ?: mapOf()
        }
    }

    suspend fun parseParam(param: List<MediationConfiguration>): List<String> {
        return withContext(Dispatchers.Default) {
            val listAppKey = arrayListOf<String>()
            param.asFlow().catch {}
                .onEach {
                    it.serverParameters.getString("parameter")
                        ?.let { it1 ->
                            parseParam(it1)
                        }?.let { map ->
                            if (map.containsValue(IKCustomConst.APP_KEY) && !map[IKCustomConst.APP_KEY].isNullOrBlank()) {
                                map[IKCustomConst.APP_KEY]?.let { it1 ->
                                    listAppKey.add(it1.trim())
                                }
                            }
                            listAppKey.distinct()
                        }
                }
                .flowOn(Dispatchers.Default)
                .collect()
            return@withContext listAppKey
        }
    }

    suspend fun getAdUnit(param: MediationConfiguration): String? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val gson = Gson()
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                return@withContext gson.fromJson<Map<String, String>>(
                    param.serverParameters.getString("parameter"),
                    mapType
                )[IKCustomConst.UNIT_ID]?.trim()
            }.getOrNull()
        }
    }

    suspend fun getAppKey(param: MediationAdConfiguration): String? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val gson = Gson()
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                return@withContext gson.fromJson<Map<String, String>>(
                    param.serverParameters.getString("parameter"),
                    mapType
                )[IKCustomConst.APP_KEY]?.trim()
            }.getOrNull()
        }
    }

    suspend fun getAdUnit(param: Bundle): String? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val gson = Gson()
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                return@withContext gson.fromJson<Map<String, String>>(
                    param.getString("parameter"),
                    mapType
                )[IKCustomConst.UNIT_ID]?.trim()
            }.getOrNull()
        }
    }

    suspend fun getPricePoint(param: Bundle): String? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val gson = Gson()
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                return@withContext gson.fromJson<Map<String, String>>(
                    param.getString("parameter"),
                    mapType
                )[IKCustomConst.PRICE_POINT]?.trim()
            }.getOrNull()
        }
    }

    suspend fun getAppKey(param: Bundle): String? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val gson = Gson()
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                return@withContext gson.fromJson<Map<String, String>>(
                    param.getString("parameter"),
                    mapType
                )[IKCustomConst.APP_KEY]?.trim()
            }.getOrNull()
        }
    }
}