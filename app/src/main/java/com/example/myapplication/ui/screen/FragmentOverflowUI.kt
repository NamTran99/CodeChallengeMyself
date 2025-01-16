package com.example.myapplication.ui.screen

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.data.services.MainRemoteService
import com.example.myapplication.databinding.FragmentOverflowUiBinding
import com.example.myapplication.databinding.FragmentTestUiBinding
import com.example.myapplication.extensions.formatInputToDecimalPlaces
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentOverflowUI : BaseFragment<FragmentOverflowUiBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_overflow_ui


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity(). window.statusBarColor = android.graphics.Color.BLUE
        initView()
    }

    var mIsEnable = false

    private fun a(){
        mIsEnable = !mIsEnable
        val windowInsets = ViewCompat.getRootWindowInsets(requireActivity().window.decorView)
        val statusBarHeight = windowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        val navigationBarHeight = windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0

        requireActivity()?.apply {
            binding.apply {
                if (mIsEnable) {
                    window.statusBarColor = android.graphics.Color.BLUE
//                    lvLayout.fitsSystemWindows =true
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    Log.d("TAG", "a: NamTD8")

                    view!!.setPadding(
                        view!!.paddingLeft,
                        0,
                        view!!.paddingRight,
                       0
                    )
                } else {
                    window.statusBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    Log.d("TAG", "a: NamTD88")

                    ViewCompat.setOnApplyWindowInsetsListener(view!!) { v, insets ->
                        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        v.setPadding(
                            v.paddingLeft,
                            systemBarsInsets.top,  // Add top padding for the status bar
                            v.paddingRight,
                            systemBarsInsets.bottom  // Add bottom padding for the navigation bar
                        )
                        insets
                    }


                }
            }
            // Configure the behavior of the hidden system bars.
//            windowInsetsController.systemBarsBehavior =
//                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        }
    }

    private fun initView() {
        binding.apply {
            abc.setOnClickListener {
                a()
            }
        }
    }

}