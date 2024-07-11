package com.example.myapplication.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.databinding.ViewAiHelperBinding
import com.example.myapplication.extensions.fadeInFromRight
import com.example.myapplication.extensions.show


interface IAiHelperCallback {
    fun onClickTranslator()
    fun onClickWriter(viewAiWriterOptions: ViewAiWriterOptions)
    fun onClickChecker()
}

class AiHelperVIew(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {

    private var callbackClient: IAiHelperCallback? = null

    private val binding =
        ViewAiHelperBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
        initView()
    }

    private var isExpand = false
        set(value) {
            field = value
            binding.apply {
                if (isExpand)
                    lvExpand.fadeInFromRight()
                else
                    lvCollapse.fadeInFromRight()
                lvExpand.show(isExpand)
                lvCollapse.show(!isExpand)
            }
        }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("TAG", "dispatchTouchEvent: NamTD8")
        return super.dispatchTouchEvent(ev)
    }

    private fun initView() {
        binding.apply {
            root.isFocusable = true
            root.setOnFocusChangeListener { v, hasFocus ->
                Log.d("TAG", "initView focus: NamTD8")
                if (!hasFocus) isExpand = false
            }
            imageView.setOnClickListener {
                lvMain.callOnClick()
            }
            lvMain.setOnClickListener {
                root.isClickable = !isExpand
                isExpand = isExpand.not()
            }
        }
    }

    fun setIsExpandView(isExpand: Boolean) {
        this.isExpand = isExpand
    }

    fun setCallBackClient(callbackClient: IAiHelperCallback) {
        this.callbackClient = callbackClient
    }

}