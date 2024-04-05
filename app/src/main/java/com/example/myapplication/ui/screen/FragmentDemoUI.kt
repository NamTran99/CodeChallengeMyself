package com.example.myapplication.ui.screen

import android.os.Bundle
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentDemoUiBinding
import com.example.myapplication.databinding.FragmentShimmerLayoutBinding
import com.example.myapplication.extensions.moveViewFromBottom

class FragmentDemoUI : BaseFragment<FragmentDemoUiBinding>(){
    override val layoutId: Int
        get() = R.layout.fragment_demo_ui

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            rootLayout.moveViewFromBottom(1000)
        }
    }
}