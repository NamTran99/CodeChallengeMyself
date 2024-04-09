package com.example.myapplication.ui.screen

import android.os.Bundle
import android.view.View
import androidx.core.graphics.rotationMatrix
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentDemoHandyBoxBinding
import com.example.myapplication.databinding.FragmentDemoUiBinding
import com.example.myapplication.databinding.FragmentShimmerLayoutBinding
import com.example.myapplication.extensions.moveViewFromBottom
import com.example.myapplication.extensions.setOnSafeClickListener
import com.example.myapplication.ui.widget.HandyBoxData
import com.example.myapplication.ui.widget.HandyBoxStatus

class FragmentBot : BaseFragment<FragmentDemoHandyBoxBinding>(){
    override val layoutId: Int
        get() = R.layout.fragment_demo_handy_box

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val status = listOf(HandyBoxStatus.Welcome, HandyBoxStatus.Loading, HandyBoxStatus.Success, HandyBoxStatus.Error)
        var index = 0
        binding.apply {
            btChangeStatus.setOnSafeClickListener {
                handyBox.setData(
                    data = HandyBoxData(
                        status = status[index%4],
                        errorContent = R.string.txt_error
                    )
                )
                index += 1
            }
        }
    }
}