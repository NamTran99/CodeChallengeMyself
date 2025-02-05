package com.example.ads.activity.core

import javax.crypto.Cipher

object CipherHelper {
    @JvmStatic
    fun doFinal(cipher: Cipher, input: ByteArray?): ByteArray? {
        return try {
            cipher.doFinal(input)
        } catch (e: Throwable) {
            return byteArrayOf()
        }
    }

}