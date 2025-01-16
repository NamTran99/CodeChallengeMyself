package com.example.myapplication.core.platform

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

abstract class BaseFragment<T: ViewDataBinding>: Fragment() {
    private val TAG: String = this::class.java.simpleName
    internal  lateinit var binding: T

    @get:LayoutRes
    abstract val layoutId: Int
    open fun onBackPressed() {}

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "namescreen: NamTD8 ${this::class.simpleName}")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
        super.onViewCreated(view, savedInstanceState)
    }


    open fun onBackFragment() {
        Log.d(TAG, "onBackFragment: ")
        val navHostFragment = this.parentFragment as? NavHostFragment
        if (navHostFragment != null && navHostFragment.childFragmentManager.backStackEntryCount == 0) {
//            baseActivity?.backTwoTimeCloseApp()
        } else {
//            (binding.root as? ViewGroup)?.removeAllViews()
//            findNavController().popBackStack()
        }
    }

}