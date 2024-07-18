package com.example.myapplication.core.recyclerview

import android.annotation.SuppressLint

open class KeyCallbackItem<MODEL : KeyModel> : BaseCallbackItem<MODEL>() {
    override fun areItemsTheSame(oldItem: MODEL, newItem: MODEL): Boolean {
        return oldItem.identity == newItem.identity
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: MODEL, newItem: MODEL): Boolean {
        return if (oldItem::class.isData) {
            oldItem == newItem
        } else {
            super.areContentsTheSame(oldItem, newItem)
        }
    }
}