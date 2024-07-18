package com.example.myapplication.ui.screen




import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentDemoUiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FragmentDemoUI : BaseFragment<FragmentDemoUiBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_demo_ui

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
        }
    }
}
