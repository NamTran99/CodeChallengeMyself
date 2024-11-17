package com.example.myapplication.core.platform

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            object : BottomSheetDialog(requireContext(), R.style.CustomDialogTheme) {

            }
        dialog.let {
            it.setOnShowListener {

            }
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val layoutParams = sheet.layoutParams
                layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
                sheet.layoutParams = layoutParams
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true
        if (!dialogAppBehavior.isDraggable) {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog.behavior
            bottomSheetBehavior.isDraggable = false
        }
        if (dialogAppBehavior.fixBugNotShowAllView) {



            dialog?.setOnShowListener {
                val dialog = it as BottomSheetDialog
                val bottomSheet =
                    dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    val layoutParams = sheet.layoutParams
                    layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
                    bottomSheet.layoutParams = layoutParams
                    dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    sheet.parent.parent.requestLayout()
                }
            }
        }
    }

    fun hideKeyboard() {
        val inputMethodManager: InputMethodManager? =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun observeFlowOnStart(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                block(this)
            }
        }
    }

}