package com.example.myapplication.ui.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTestUiBinding
import com.example.mybase.core.platform.BaseFragment
import com.example.mybase.extensions.formatInputToDecimalPlaces
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentTestUI : BaseFragment<FragmentTestUiBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_test_ui

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.apply {
            edtInputField.formatInputToDecimalPlaces("$","2",5){
                Log.d("TAG", "initView-NamTD8: ${it}")
            }
        }
    }

}