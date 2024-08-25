package com.example.myapplication.ui.screen

import android.os.Bundle
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentAiGeneratorBoxBinding

class FragmentAiGeneratorBox : BaseFragment<FragmentAiGeneratorBoxBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_ai_generator_box

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}