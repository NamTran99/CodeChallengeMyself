package com.example.myapplication.ui.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentOnTouchViewBinding
import com.example.mybase.core.platform.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentTestCallApi : BaseFragment<FragmentOnTouchViewBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_on_touch_view


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
//            Log.d("TAG", "onViewCreated NamTD8: $a")
        }
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
                it.isClickable = false
                it.isFocusable = false
            }
        }
    }

}