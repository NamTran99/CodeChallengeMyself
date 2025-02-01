package com.example.myapplication.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.databinding.LayoutCustomTextSwitcherBinding
import com.example.mybase.extensions.setOnSafeClickListener

class CustomTextSwitcher @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    private var binding: LayoutCustomTextSwitcherBinding
    private var selectedPosition = 0
    private var onSelectListener: (Int) -> Unit = { }

    init {
        binding = LayoutCustomTextSwitcherBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
        initListener()
    }

    private fun initView() {
        binding.apply {
            tvLeft.isSelected = true
        }
    }

    private fun initListener() {
        binding.apply {
            tvLeft.setOnClickListener {
                select(0)
            }
            tvRight.setOnSafeClickListener {
                select(1)
            }
        }
    }

    fun setOnSelectListener(listener: (Int) -> Unit) {
        onSelectListener = listener
    }

    fun select(position: Int, isNotifyListener: Boolean = true) {
        selectedPosition = position
        binding.apply {
            tvLeft.isSelected = position == 0
            tvRight.isSelected = position == 1
        }
        if (isNotifyListener) {
            onSelectListener.invoke(position)
        }
    }

    fun isTickerSelected(): Boolean {
        return selectedPosition == 0
    }
}
