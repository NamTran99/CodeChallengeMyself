package com.example.mybase.core.platform.storage

import android.content.Context
import android.util.Log
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
        scopeSupervisorIO.launchWithSupervisorJob {
            dataStore.edit { preferences ->
                when (value) {
                    is String -> preferences[stringPreferencesKey(key)] = value
                    is Int -> preferences[intPreferencesKey(key)] = value
                    is Boolean -> preferences[booleanPreferencesKey(key)] = value
                    is Float -> preferences[floatPreferencesKey(key)] = value
                    is Long -> preferences[longPreferencesKey(key)] = value
                    else -> {
                        preferences[stringPreferencesKey(key)] = Gson().toJson(value)
                    }
                }
            }
        }
    }

    // Read any supported type from DataStore
    inline fun <reified T> getData(key: String, defaultValue: T? = null): Flow<T?> {
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
                    String::class -> preferences[stringPreferencesKey(key)] ?: defaultValue
                    Int::class -> preferences[intPreferencesKey(key)] ?: defaultValue
                    Boolean::class -> preferences[booleanPreferencesKey(key)] ?: defaultValue
                    Float::class -> preferences[floatPreferencesKey(key)] ?: defaultValue
                    Long::class -> preferences[longPreferencesKey(key)] ?: defaultValue
                    else -> preferences[stringPreferencesKey(key)]?.let {
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

}