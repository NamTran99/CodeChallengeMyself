package com.example.myapplication.ui.screen

import android.os.Bundle
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentEditTextExtensionBinding
import com.example.myapplication.databinding.FragmentShimmerLayoutBinding
import com.example.myapplication.extensions.formatInputToDecimalPlaces
import java.lang.reflect.Field

class FragmentShimmerLayout : BaseFragment<FragmentShimmerLayoutBinding>(){
    override val layoutId: Int
        get() = R.layout.fragment_shimmer_layout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
//                shimmerLayout.star
        }
    }
}