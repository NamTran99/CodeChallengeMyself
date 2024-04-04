package com.example.myapplication

import com.example.myapplication.core.platform.BaseRecycler
import com.example.myapplication.databinding.ItemTestBinding

data class MyModel(var a1: String)

class ItemAdapter: BaseRecycler<MyModel, ItemTestBinding>() {
    override val layoutId: Int
        get() = R.layout.item_test

    override fun onBindHolder(item: MyModel, binding: ItemTestBinding, adapterPosition: Int) {
        binding.apply {
            tvContent.text = item.a1
        }
    }
}