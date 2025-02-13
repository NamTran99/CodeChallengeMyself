package com.example.myapplication.service.socket

import android.util.Base64
import java.security.SecureRandom
import java.util.Locale

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
}