package com.example.myapplication.extensions

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Build.VERSION_CODES.N
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.myapplication.ui.widget.SlideToActView

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

fun ImageView.toggleImage(isActive: Boolean, activeImage: Int, inActiveImage: Int, onClickListener: ((Boolean) -> Unit )? = null){
    var mIsActive = isActive
    setImageResource(if(mIsActive) activeImage else inActiveImage)
    setOnClickListener {
        mIsActive = !isActive
        setImageResource(if(isActive) activeImage else inActiveImage)
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

@SuppressLint("UseCompatLoadingForDrawables")
internal fun loadIconCompat(
    context: Context,
    value: Int,
): Drawable {
    // Due to bug in the AVD implementation in the support library, we use it only for API < 21
    return if (SDK_INT >= LOLLIPOP) {
        context.resources.getDrawable(value, context.theme)
    } else {
        return AnimatedVectorDrawableCompat.create(context, value)
            ?: ContextCompat.getDrawable(context, value)!!
    }
}

internal fun tintIconCompat(
    icon: Drawable,
    color: Int,
) {
    // Tinting the tick with the proper implementation method
    when {
        SDK_INT >= LOLLIPOP -> icon.setTint(color)
        else -> DrawableCompat.setTint(icon, color)
    }
}

/**
 * Internal method to start the Icon AVD animation, with the proper library based on API level.
 */
internal fun startIconAnimation(icon: Drawable) {
    when {
        SDK_INT >= LOLLIPOP && icon is AnimatedVectorDrawable -> icon.start()
        icon is AnimatedVectorDrawableCompat -> icon.start()
        else -> {
            // Do nothing as the icon can't be animated
        }
    }
}

/**
 * Internal method to stop the Icon AVD animation, with the proper library based on API level.
 */
internal fun stopIconAnimation(icon: Drawable) {
    when {
        SDK_INT >= LOLLIPOP && icon is AnimatedVectorDrawable -> icon.stop()
        icon is AnimatedVectorDrawableCompat -> icon.stop()
        else -> {
            // Do nothing as the icon can't be animated
        }
    }
}

/**
 * Creates a [ValueAnimator] to animate the complete icon. Uses the [fallbackToFadeAnimation]
 * to decide if the icon should be animated with a Fade or with using [AnimatedVectorDrawable].
 */
fun createIconAnimator(
    view: SlideToActView,
    icon: Drawable,
    listener: ValueAnimator.AnimatorUpdateListener,
): ValueAnimator {
    if (fallbackToFadeAnimation(icon)) {
        // Fallback not using AVD.
        val tickAnimator = ValueAnimator.ofInt(0, 255)
        tickAnimator.addUpdateListener(listener)
        tickAnimator.addUpdateListener {
            icon.alpha = it.animatedValue as Int
            view.invalidate()
        }
        return tickAnimator
    } else {
        // Used AVD Animation.
        val tickAnimator = ValueAnimator.ofInt(0)
        var startedOnce = false
        tickAnimator.addUpdateListener(listener)
        tickAnimator.addUpdateListener {
            if (!startedOnce) {
                startIconAnimation(icon)
                view.invalidate()
                startedOnce = true
            }
        }
        return tickAnimator
    }
}

/**
 * Logic to decide if we should do a Fade or use the [AnimatedVectorDrawable] animation.
 */
private fun fallbackToFadeAnimation(icon: Drawable) =
    when {
        // We don't use AVD at all for <= N.
        SDK_INT <= N -> true
        SDK_INT >= LOLLIPOP && icon !is AnimatedVectorDrawable -> true
        SDK_INT < LOLLIPOP && icon !is AnimatedVectorDrawableCompat -> true
        else -> false
    }