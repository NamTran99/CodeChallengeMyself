package com.example.myapplication.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.myapplication.databinding.ViewAiWriterOptionsBinding
import com.example.myapplication.extensions.show

interface IAiWriterOptionsCallback {
    fun onClickRemove(view: ViewAiWriterOptions)
}

class ViewAiWriterOptions(context: Context, attributeSet: AttributeSet? = null) :
    LinearLayout(context, attributeSet) {

    enum class Type {
        TextOnly, TextAndRemove
    }


    private val binding =
        ViewAiWriterOptionsBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    var callback: IAiWriterOptionsCallback? = null

    init {
        initView()
    }

    private fun initView() {
        binding.apply {
            imgRemove.setOnClickListener {
                callback?.onClickRemove(this@ViewAiWriterOptions)
            }
        }
    }

    private var data: String = ""
        get() = binding.edtContent.text.toString()
        set(value) {
            field = value
            binding.edtContent.setText(value)
        }

    private var type: Type = Type.TextOnly
        set(value) {
            field = value
            binding.apply {
                imgRemove.show(type == Type.TextAndRemove)
            }
        }

    fun setCallBackClient(callback: IAiWriterOptionsCallback) {
        this.callback = callback
    }

    fun getContent() = data

    fun setViewType(type: Type) {
        this.type = type
    }

    fun setHint(hint: Int){
        binding.edtContent.setHint(hint)
    }
}