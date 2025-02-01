package com.example.ads.activity.core

import android.app.Activity
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.example.ads.activity.utils.IKSdkDefConst
import java.lang.reflect.Type


object SDKDataHolder {
    init {
        initLib()
    }

    private var initLib = false
    fun initLib() {
        if (initLib)
            return
        kotlin.runCatching {
            System.loadLibrary("nativelib")
            initLib = true
        }
    }

    external fun getFlag(): Int
    external fun getBnnFlag(): String
    external fun getPosFlag(pos: String): String

    @Throws(Throwable::class)
    external fun <T> getObject(encoded: String?, classOfT: Class<T>?): T?

    @Throws(Throwable::class)
    external fun <T> getObject(encoded: String?, typeOfT: Type?): T?

    @Throws(Throwable::class)
    external fun <T> getObjectSdk(encoded: String?, classOfT: Class<T>?): T?

    @Throws(Throwable::class)
    external fun <T> getObjectSdk(encoded: String?, typeOfT: Type?): T?

    @Throws(Throwable::class)
    external fun <T> getObjectDb(encoded: String?, classOfT: Class<T>?): T?

    @Throws(Throwable::class)
    external fun <T> getObjectDb(encoded: String?, typeOfT: Type?): T?

    @Throws(Throwable::class)
    external fun <T> encryptObjectDb(encoded: T?, typeOfT: Type?): String?


    external fun getDefaultString(
        encoded: String?
    ): String?

    object FFun {
        fun getFlag(): String = runCatching {
            getCFlag()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getInDKey(): String = runCatching {
            getCInDKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getHaDKey(): String = runCatching {
            getCHaDKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getActDKey(): String = runCatching {
            getCActDKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getActCLKey(): String = runCatching {
            getCActCLKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getActOutCLKey(): String = runCatching {
            getCActOutCLKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getActCLNKey(): String = runCatching {
            getCActCLNKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getFValid(value: String?, vaCh: Boolean): Boolean = runCatching {
            getCFValid(value ?: IKSdkDefConst.EMPTY, vaCh)
        }.getOrNull() ?: false

        fun dLoc(value: String?): Boolean = runCatching {
            dCLoc(value?.lowercase() ?: IKSdkDefConst.EMPTY)
        }.getOrNull() ?: false

        fun NotificationCompat.Builder.ad(itn: PendingIntent): Boolean {
            return runCatching {
                adc(this, itn)
                true
            }.getOrNull() ?: false
        }

        fun dffC() {
            runCatching {
                pxx()
            }
        }

        fun daCUmFxC(): Boolean {
            return runCatching {
                pxxCxx()
            }.getOrNull() ?: false
        }

        fun umFdXC(activity: Activity?, callback: (() -> Unit)): Boolean {
            return runCatching {
                pxxCxxFx(activity, callback)
            }.getOrNull() ?: false
        }

        fun getCBtTtKeyC(): String = runCatching {
            getCBtTtKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getCBtClKeyC(): String = runCatching {
            getCBtClKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getCBgClKeyC(): String = runCatching {
            getCBgClKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getCTClKeyC(): String = runCatching {
            getCTClKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getCTtKeyC(): String = runCatching {
            getCTtKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getCBdKeyC(): String = runCatching {
            getCBdKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getCImKeyC(): String = runCatching {
            getCImKey()
        }.getOrNull() ?: IKSdkDefConst.EMPTY

        fun getWLVLC(value: String?): Boolean = runCatching {
            getWLVL(value ?: IKSdkDefConst.EMPTY)
        }.getOrNull() ?: false

        external fun getCFlag(): String
        external fun getCInDKey(): String
        external fun getCHaDKey(): String
        external fun getCActDKey(): String
        external fun getCActCLKey(): String
        external fun getCActOutCLKey(): String
        external fun getCActCLNKey(): String
        external fun getCFValid(value: String, vaCh: Boolean): Boolean
        external fun dCLoc(value: String): Boolean
        external fun adc(par: NotificationCompat.Builder, value: PendingIntent): Boolean
        external fun pxx()
        external fun pxxCxx(): Boolean
        external fun pxxCxxFx(activity: Activity?, callback: (() -> Unit)): Boolean
        external fun getCBtTtKey(): String
        external fun getCBtClKey(): String
        external fun getCBgClKey(): String
        external fun getCTClKey(): String
        external fun getCTtKey(): String
        external fun getCBdKey(): String
        external fun getCImKey(): String

        external fun getWLVL(value: String): Boolean
    }
}