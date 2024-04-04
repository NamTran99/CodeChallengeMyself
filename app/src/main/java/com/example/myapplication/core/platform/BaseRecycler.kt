package com.example.myapplication.core.platform

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecycler<T: Any, VB:ViewDataBinding>: RecyclerView.Adapter<ItemViewHolder<VB>>() {

    @get:LayoutRes
    abstract val layoutId: Int

    var mList = listOf<T>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<VB> {
        return ItemViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false)
        )
    }

    override fun getItemCount(): Int= mList.size

    override fun onBindViewHolder(holder: ItemViewHolder<VB>, position: Int) {
        onBindHolder(mList[position], holder.binding, position)
    }

    protected abstract fun onBindHolder(item: T, binding: VB, adapterPosition: Int)
}

class ItemViewHolder<VB:ViewDataBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root)
