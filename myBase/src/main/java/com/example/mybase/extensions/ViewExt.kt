package com.example.mybase.extensions

import android.content.Context
import android.content.res.TypedArray
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.core.view.isInvisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

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

fun View.hide(b: Boolean = true) {
    if (b) {
        visibility = View.GONE
    } else {
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

fun Context.loadAttrs(attrs: AttributeSet?, attrType: IntArray, function: TypedArray.() -> Unit) {
    if (attrs == null) return
    val a = obtainStyledAttributes(attrs, attrType)
    function(a)
    a.recycle()
}

fun ImageView.toggleImage(
    isActive: Boolean,
    activeImage: Int,
    inActiveImage: Int,
    onClickListener: ((Boolean) -> Unit)? = null
) {
    var mIsActive = isActive
    setImageResource(if (mIsActive) activeImage else inActiveImage)
    setOnClickListener {
        mIsActive = !isActive
        setImageResource(if (isActive) activeImage else inActiveImage)
        onClickListener?.invoke(mIsActive)
    }
}

fun <T : ViewDataBinding> ViewGroup.inflateBinding(
    @LayoutRes layoutId: Int,
    attach: Boolean = false,
): T {
    return DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, this, attach)
}

private fun RecyclerView.smoothScrollToPositionAtTop(position: Int) {
    val layoutManager = this.layoutManager as? LinearLayoutManager ?: return

    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun calculateDyToMakeVisible(view: View?, snapPreference: Int): Int {
            return -(view?.top ?: 0)
        }
    }

    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}

private fun RecyclerView.smoothScrollItemToCenter(position: Int) {
    val layoutManager = this.layoutManager as? LinearLayoutManager ?: return

    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun calculateDyToMakeVisible(view: View?, snapPreference: Int): Int {
            return 0
        }

        override fun calculateDxToMakeVisible(view: View?, snapPreference: Int): Int {
            val parentCenter = width / 2
            val childCenter = (view?.left ?: 0) + (view?.width ?: 0) / 2
            return parentCenter - childCenter
        }
    }

    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}
