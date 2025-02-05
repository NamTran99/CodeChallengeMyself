package com.tdt.pmobile3.ewallet.base.recyclerview.core


import androidx.databinding.ViewDataBinding
import com.tdt.pmobile3.ewallet.base.recyclerview.adapter.AbstractDataBindingViewHolder

open class DataBindingViewHolder<T : KeyModel, DB : ViewDataBinding> private constructor(
    binding: DB,
) : AbstractDataBindingViewHolder<DB>(
    binding
) {
    open fun bind(block: DB.() -> Unit) {
        block(binding)
    }

    companion object {
        fun <T : KeyModel, DB : ViewDataBinding> from(
            binding: DB,
        ): DataBindingViewHolder<T, DB> {
            return DataBindingViewHolder(binding)
        }
    }
}