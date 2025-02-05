package com.example.myapplication.ui.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.mybase.extensions.CallBackNoParam

class Typewriter : AppCompatTextView {
    var isDoneAnimate  = true

    private var content: CharSequence? = null
    private var mIndex = 0
    private var mDelay: Long = 3 //Default 500ms delay
    private var callbackDone: CallBackNoParam? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    private val mHandler = Handler(Looper.getMainLooper())
    private val characterAdder: Runnable = object : Runnable {
        override fun run() {
            text = content!!.subSequence(0, mIndex++)
            if (mIndex <= content!!.length) {
                mHandler.postDelayed(this, mDelay)
            }else{
                isDoneAnimate = true
                callbackDone?.invoke()
            }
        }
    }

    fun animateText(text: CharSequence?, callbackDone: (() -> Unit)? = null) {
        this.callbackDone = callbackDone
        content = text
        mIndex = 0
        setText("")
        isDoneAnimate = false
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun forceShowFullContent(){
        if(!isDoneAnimate){
            isDoneAnimate = true
            mHandler.removeCallbacks(characterAdder)
            setText(content)
            callbackDone?.invoke()
        }
    }

    fun setCharacterDelay(millis: Long) {
        mDelay = millis
    }
}
