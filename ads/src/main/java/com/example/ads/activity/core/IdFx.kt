package com.example.ads.activity.core

import android.app.Activity

object IdFx {
    @JvmStatic
    fun pxx(vl: String) {
        IdFxImp.pxx(vl)
    }

    @JvmStatic
    fun pxxCxx(vl: String): Boolean {
        return IdFxImp.pxxCxx(vl)
    }

    @JvmStatic
    fun pxxCxxIm(activity: Activity?, callback: (() -> Unit)) {
        return IdFxImp.txCDmFxIm(activity, callback)
    }

}