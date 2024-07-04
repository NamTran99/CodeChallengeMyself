package com.example.myapplication.ui.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class OutsideClickConstraintLayout(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    private var viewOutsideClickListenerMap = mutableMapOf<View, () -> Unit>()

    fun setOnOutsideClickListenerForView(view: View, listener: () -> Unit) {
        viewOutsideClickListenerMap[view] = listener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        viewOutsideClickListenerMap.forEach { (view, function) ->
            if (isMotionEventOutsideView(view, ev)) function.invoke()
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun isMotionEventOutsideView(view: View, motionEvent: MotionEvent): Boolean {
        val viewRectangle = Rect()
        view.getGlobalVisibleRect(viewRectangle)
        return !viewRectangle.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())
    }
}