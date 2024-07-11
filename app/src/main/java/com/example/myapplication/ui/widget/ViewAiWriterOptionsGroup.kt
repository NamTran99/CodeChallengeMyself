package com.example.myapplication.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.myapplication.R


class ViewAiWriterOptionsGroup(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet), IAiWriterOptionsCallback {

    val data: List<String> = listOf()

    private var length = 0
    private val listHint =
        listOf(R.string.hint_ai_writer_1, R.string.hint_ai_writer_2, R.string.hint_ai_writer_3)

    var isSetRandomHint = true

    init {
        orientation = VERTICAL

        this.addView(ViewAiWriterOptions(this.context).apply {
            setViewType(ViewAiWriterOptions.Type.TextOnly)
        })
    }

    fun addMoreOptionView(number: Int = 1) {
        this.addView(ViewAiWriterOptions(context).apply {
            length++
            setViewType(ViewAiWriterOptions.Type.TextAndRemove)
            if (isSetRandomHint && length < listHint.count()) {
                setHint(listHint[length])
            }
            setCallBackClient(this@ViewAiWriterOptionsGroup)
        })
    }

    override fun onClickRemove(view: ViewAiWriterOptions) {
        this.removeView(view)
    }
}