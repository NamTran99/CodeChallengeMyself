package com.example.ads.activity.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext


object IKSdkDataStoreBilling {

    suspend fun putLong(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.putLong(key, value)
        }
    }

    fun getLongFlow(key: String, default: Long): Flow<Long> = flow {
        emit(IKSdkDataStoreBillingCore.getLong(key, default))
    }

    suspend fun getLong(key: String, default: Long): Long {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.getLong(key, default)
        }
    }

    suspend fun putInt(key: String, value: Int) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.putInt(key, value)
        }
    }

    fun getIntFlow(key: String, default: Int): Flow<Int> = flow {
        emit(IKSdkDataStoreBillingCore.getInt(key, default))
    }

    suspend fun getInt(key: String, default: Int): Int {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.getInt(key, default)
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.putBoolean(key, value)
        }
    }

    fun getBooleanFlow(key: String, default: Boolean): Flow<Boolean> = flow {
        emit(IKSdkDataStoreBillingCore.getBoolean(key, default))
    }

    suspend fun getBoolean(key: String, default: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.getBoolean(key, default)
        }
    }

    suspend fun putString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.putString(key, value)
        }
    }

    fun getStringFlow(key: String, default: String): Flow<String> = flow {
        emit(IKSdkDataStoreBillingCore.getString(key, default))
    }

    suspend fun getString(key: String, default: String): String {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.getString(key, default)
        }
    }

    suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreBillingCore.remove(key)
        }
    }
}

