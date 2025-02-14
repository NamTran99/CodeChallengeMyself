package com.example.myapplication.service.socket

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import android.provider.Settings
import java.util.TimeZone

object SocketHelper {
    fun generateWebSocketKey(): String {
        val randomBytes = ByteArray(16)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP)
    }

    fun getLanguage(): String = Locale.getDefault().toString()

    fun extractAndRemoveLeadingNumbers(text: String): Pair<String, String> {
        val numbers = text.takeWhile { it.isDigit() }
        val remainingText = text.drop(numbers.length)
        return Pair(numbers, remainingText)
    }

    fun extractCodeAndJsonContent(response: String): Pair<String, String> {
        val numbers = response.takeWhile { it.isDigit() }
        val jsonContent = extractJsonString(response)?:""
        return Pair(numbers, jsonContent)
    }

    fun <T> formatMessage(id: String, vararg data: T): String {
        val gson = Gson()
        val jsonData = gson.toJson(data.toList())
        return "$id$jsonData"
    }

    fun genUUID() = UUID.randomUUID().toString()

    @SuppressLint("HardwareIds")
    fun getAndroidDeviceId(context: Context?): String {
        return context?.let{
            Settings.Secure.getString(it.contentResolver, Settings.Secure.ANDROID_ID)
        }?: ""
    }

    fun getDeviceTimeZone(): String {
        return TimeZone.getDefault().id
    }

    fun extractJsonString(rawString: String): String? {
        val jsonStart = rawString.indexOf("{")
        if (jsonStart == -1) return null
        return rawString.substring(jsonStart, rawString.lastIndexOf("}") + 1)
    }
}