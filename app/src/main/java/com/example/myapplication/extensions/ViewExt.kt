package com.example.myapplication.extensions

import android.os.SystemClock
import android.view.View
import androidx.core.view.isInvisible

fun <T : View> T.show(b: Boolean = true, function: T.() -> Unit = {}) {
    visibility = if (b) {
        function()
        View.VISIBLE
    } else View.GONE
}

fun <T : View> T.invisible(b: Boolean = true, function: T.() -> Unit = {}) {
    isInvisible = if (b) {
        function()
        true
    } else false
}

fun <T : View> T.visible(b: Boolean = true, function: T.() -> Unit = {}) {
    visibility = if (b) {
        function()
        View.VISIBLE
    } else View.INVISIBLE
}

fun View.hide(b: Boolean) {
    if(b){
        visibility = View.GONE
    }else{
        visibility = View.VISIBLE
    }
}

class SafeClickListener(
    private var defaultInterval: Int = 600,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(view: View) {
        if ((SystemClock.elapsedRealtime() - lastTimeClicked) < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(view)
    }
}


fun View.setOnSafeClickListener(onSafeClick: (View) -> Unit) {
    setOnClickListener(
        SafeClickListener {
            onSafeClick(it)
        }
    )
}

