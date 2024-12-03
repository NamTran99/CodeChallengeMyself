package com.example.myapplication.pglCryptUtils

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

//class PglCryptUtils private constructor() {
//    companion object {
//        const val BASE64_FAILED = 504
//        const val COMPRESS_FAILED = 503
//        const val CRYPT_OK = 0
//        const val CYPHER_VERSION = 4
//        const val DECRYPT_FAILED = 506
//        const val ENCRYPT_FAILED = 505
//        const val INPUT_INVALID = 502
//        const val KEY_CYPHER = "cypher"
//        const val KEY_MESSAGE = "message"
//        const val LOAD_SO_FAILED = 501
//
//        @Volatile
//        private var isLibraryLoaded = true
//
//        @Volatile
//        private var instance: PglCryptUtils? = null
//
//        init {
//            try {
//                System.loadLibrary("pglarmor")
//            } catch (e: Throwable) {
//                isLibraryLoaded = false
//            }
//        }
//
//        @JvmStatic
//        fun getInstance(): PglCryptUtils {
//            return instance ?: synchronized(this) {
//                instance ?: PglCryptUtils().also { instance = it }
//            }
//        }
//
//        @JvmStatic
//        private external fun bc(operation: Int, input: ByteArray): ByteArray?
//
//        private fun compress(input: String?): ByteArray? {
//            if (input.isNullOrEmpty()) return null
//            return try {
//                ByteArrayOutputStream().use { byteArrayOutputStream ->
//                    GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
//                        gzipOutputStream.write(input.toByteArray(Charsets.UTF_8))
//                    }
//                    byteArrayOutputStream.toByteArray()
//                }
//            } catch (e: Exception) {
//                Log.e("ARMOR", e.toString())
//                null
//            }
//        }
//
//        private fun decompress(input: ByteArray?): String? {
//            if (input == null || input.isEmpty()) return null
//            return try {
//                ByteArrayInputStream(input).use { byteArrayInputStream ->
//                    GZIPInputStream(byteArrayInputStream).use { gzipInputStream ->
//                        ByteArrayOutputStream().use { byteArrayOutputStream ->
//                            val buffer = ByteArray(1024)
//                            var read: Int
//                            while (gzipInputStream.read(buffer).also { read = it } != -1) {
//                                byteArrayOutputStream.write(buffer, 0, read)
//                            }
//                            byteArrayOutputStream.toString(Charsets.UTF_8.name())
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("ARMOR", e.toString())
//                null
//            }
//        }
//    }
//
//    fun cypher4Decrypt(input: String?): Pair<Int, String?> {
//        if (!isLibraryLoaded) {
//            return Pair(LOAD_SO_FAILED, null)
//        }
//        if (input.isNullOrEmpty()) {
//            return Pair(INPUT_INVALID, null)
//        }
//        val decodedBytes = Base64.decode(input, Base64.DEFAULT)
//        if (decodedBytes.isEmpty()) {
//            return Pair(BASE64_FAILED, null)
//        }
//        val decryptedBytes = try {
//            bc(1011, decodedBytes)
//        } catch (e: Throwable) {
//            Log.e("ARMOR", e.toString())
//            null
//        }
//        if (decryptedBytes == null || decryptedBytes.isEmpty()) {
//            return Pair(DECRYPT_FAILED, null)
//        }
//        val result = decompress(decryptedBytes)
//        return if (result.isNullOrEmpty()) {
//            Pair(COMPRESS_FAILED, null)
//        } else {
//            Pair(CRYPT_OK, result)
//        }
//    }
//
//    fun cypher4Encrypt(input: JSONObject?): Pair<Int, JSONObject?> {
//        if (!isLibraryLoaded) {
//            return Pair(LOAD_SO_FAILED, null)
//        }
//        if (input == null) {
//            return Pair(INPUT_INVALID, null)
//        }
//        val compressedBytes = compress(input.toString())
//        if (compressedBytes == null ||compressedBytes.isEmpty()) {
//            return Pair(COMPRESS_FAILED, null)
//        }
//        val encryptedBytes = try {
//            bc(1010, compressedBytes)
//        } catch (e: Throwable) {
//            Log.e("ARMOR", e.toString())
//            null
//        }
//        if (encryptedBytes == null || encryptedBytes.isEmpty()) {
//            return Pair(ENCRYPT_FAILED, null)
//        }
//        val encodedString = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
//        return if (encodedString.isEmpty()) {
//            Pair(BASE64_FAILED, null)
//        } else {
//            val result = JSONObject()
//            result.put(KEY_MESSAGE, encodedString)
//            result.put(KEY_CYPHER, CYPHER_VERSION)
//            Pair(CRYPT_OK, result)
//        }
//    }
//}
