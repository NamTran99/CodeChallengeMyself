package com.example.myapplication.ui.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.data.services.MainRemoteService
import com.example.myapplication.databinding.FragmentTestUiBinding
import com.example.myapplication.extensions.formatInputToDecimalPlaces
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentTestUI : BaseFragment<FragmentTestUiBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_test_ui

    @Inject
    lateinit var mainRemoteService: MainRemoteService

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