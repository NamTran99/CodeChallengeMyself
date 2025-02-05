package com.example.ads.activity.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext


object IKSdkDataStore {

    suspend fun putLong(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.putLong(key, value)
        }
    }

    fun getLongFlow(key: String, default: Long): Flow<Long> = flow {
        emit(IKSdkDataStoreCore.getLong(key, default))
    }

    suspend fun getLong(key: String, default: Long): Long {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.getLong(key, default)
        }
    }

    suspend fun putInt(key: String, value: Int) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.putInt(key, value)
        }
    }

    fun getIntFlow(key: String, default: Int): Flow<Int> = flow {
        emit(IKSdkDataStoreCore.getInt(key, default))
    }

    suspend fun getInt(key: String, default: Int): Int {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.getInt(key, default)
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.putBoolean(key, value)
        }
    }

    fun getBooleanFlow(key: String, default: Boolean): Flow<Boolean> = flow {
        emit(IKSdkDataStoreCore.getBoolean(key, default))
    }

    suspend fun getBoolean(key: String, default: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.getBoolean(key, default)
        }
    }

    suspend fun putString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.putString(key, value)
        }
    }

    fun getStringFlow(key: String, default: String): Flow<String> = flow {
        emit(IKSdkDataStoreCore.getString(key, default))
    }

    suspend fun getString(key: String, default: String): String {
        return withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.getString(key, default)
        }
    }

    suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            IKSdkDataStoreCore.remove(key)
        }
    }

}

