package com.example.myapplication.core.platform

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


abstract class BaseBottomSheetDialog<T : ViewDataBinding> : BottomSheetDialogFragment() {

    data class DialogAppBehavior(
        val isDraggable: Boolean = true,
        val dimAmount: Float = 0f,
        val fixBugNotShowAllView: Boolean = true,
    )

    open val dialogAppBehavior = DialogAppBehavior()

    lateinit var binding: T
        private set

    @get:LayoutRes
    abstract val layoutId: Int
