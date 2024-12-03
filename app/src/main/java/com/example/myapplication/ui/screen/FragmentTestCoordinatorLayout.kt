package com.example.myapplication.ui.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.children
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentAiGeneratorBoxBinding
import com.example.myapplication.databinding.FragmentCoordinatorLayoutBinding
import com.example.myapplication.databinding.FragmentOnTouchViewBinding

class FragmentTestCoordinatorLayout : BaseFragment<FragmentCoordinatorLayoutBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_coordinator_layout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        binding.apply {

        }
    }

}