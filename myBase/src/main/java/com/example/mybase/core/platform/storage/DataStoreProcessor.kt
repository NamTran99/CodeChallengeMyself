package com.example.mybase.core.platform.storage

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mybase.extensions.launchWithSupervisorJob
import com.example.mybase.extensions.scopeSupervisorIO
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore("app_preferences")

class DataStoreProcessor(private val context: Context) {

    val dataStore = context.dataStore

    // Save any supported type to DataStore
    inline fun <reified T> saveData(key: String, value: T) {
        val preferencesKey = getPreferencesKey<T>(key)
        Log.d("TAG", "saveData: NamTD8-2 ${Gson().toJson(value)} - ${value}")
        scopeSupervisorIO.launchWithSupervisorJob {
            dataStore.edit { preferences ->
                when (value) {
                    is String -> preferences[preferencesKey as Preferences.Key<String>] = value
                    is Int -> preferences[preferencesKey as Preferences.Key<Int>] = value
                    is Boolean -> preferences[preferencesKey as Preferences.Key<Boolean>] = value
                    is Float -> preferences[preferencesKey as Preferences.Key<Float>] = value
                    is Long -> preferences[preferencesKey as Preferences.Key<Long>] = value
                    else -> {
                        Log.d("TAG", "saveData: NamTD8-3 ${preferencesKey as Preferences.Key<String>}")
                        preferences[preferencesKey as Preferences.Key<String>] = Gson().toJson(value)
                    }
                }
            }
        }
    }

    // Read any supported type from DataStore
    inline fun <reified T> getData(key: String, defaultValue: T?= null): Flow<T?> {
        Log.d("TAG", "getData: NamTD8-3")
        val preferencesKey = getPreferencesKey<T>(key)
        Log.d("TAG", "getData: NamTD8-2")
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                Log.d("TAG", "getData: NamTD8-4")
                when (T::class) {
                    String::class -> preferences[preferencesKey as Preferences.Key<String>] ?: defaultValue
                    Int::class-> preferences[preferencesKey as Preferences.Key<Int>] ?: defaultValue
                    Boolean::class-> preferences[preferencesKey as Preferences.Key<Boolean>] ?: defaultValue
                    Float::class  -> preferences[preferencesKey as Preferences.Key<Float>] ?: defaultValue
                    Long::class -> preferences[preferencesKey as Preferences.Key<Long>] ?: defaultValue
                    else -> preferences[preferencesKey as Preferences.Key<String>]?.let {
                        try {
                            Gson().fromJson(it, T::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: defaultValue

                } as T?
            }
    }

    // Clear all preferences
    suspend fun clearDataStore() {
        dataStore.edit { it.clear() }
    }

    // Helper to get the Preferences.Key based on type
    inline fun <reified T> getPreferencesKey(key: String): Preferences.Key<T> {
        return when (T::class) {
            String::class -> stringPreferencesKey(key)
            Int::class -> intPreferencesKey(key)
            Boolean::class -> booleanPreferencesKey(key)
            Float::class -> floatPreferencesKey(key)
            Long::class -> longPreferencesKey(key)
            else -> stringPreferencesKey(key)
        } as Preferences.Key<T>
    }
}