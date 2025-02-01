package com.example.myapplication.ui.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.children
import com.example.myapplication.R
import com.example.mybase.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentAiGeneratorBoxBinding
import com.example.myapplication.databinding.FragmentOnTouchViewBinding

class FragmentOnTouchView : BaseFragment<FragmentOnTouchViewBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_on_touch_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        binding.apply {
            lvLayout.setOnClickListener {
                Log.d("TAG", "initView: NamTD8-1 ")
            }

            lvLayout.children.forEach {
                it.setOnClickListener {
                    Log.d("TAG", "initView: NamTD8-5 ")
                }
                it.isFocusableInTouchMode = false
                it.isClickable  =false
                it.isFocusable  = false
            }
        }
    }

}