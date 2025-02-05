package com.tdt.pmobile3.ewallet.base.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tdt.pmobile3.ewallet.base.recyclerview.core.DataBindingViewHolder
import com.tdt.pmobile3.ewallet.base.recyclerview.core.KeyCallbackItem
import com.tdt.pmobile3.ewallet.base.recyclerview.core.KeyModel

abstract class SingleHolderBindingAdapter<T : KeyModel, B : ViewDataBinding>(

) : ListAdapter<T, DataBindingViewHolder<T, B>>(KeyCallbackItem<T>()) {
    @get:LayoutRes
    protected abstract val layoutId: Int
    var onLoadMoreListener: (() -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DataBindingViewHolder<T, B> {

        val holder = DataBindingViewHolder.from<T, B>(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false)
        )
        initStateViewHolder(holder)
        return holder
    }

    override fun onBindViewHolder(
        holder: DataBindingViewHolder<T, B>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(onBindHandlePayload(holder.binding, getItem(position), position, payloads).not()){
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder<T, B>, position: Int) {
        onBind(holder.binding, getItem(position), position)
    }

    abstract fun onBind(binding: B, item: T, position: Int)
    open fun onBindHandlePayload(binding: B, model: T, position: Int, payloads: MutableList<Any>): Boolean{
        return false
    }

    open fun initStateViewHolder(holder: DataBindingViewHolder<T, B>) {
        // initial State ViewHolder
    }
}

abstract class AbstractDataBindingViewHolder<out T : ViewDataBinding>(
    val binding: T,
) : RecyclerView.ViewHolder(binding.root)