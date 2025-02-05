package com.example.myapplication.core.platform.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.mybase.core.platform.dialog.EWalletBottomSheetData
import com.example.mybase.core.platform.dialog.HeightBottomSheetType
import com.example.mybase.ui.widget.MaxHeightScrollView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog<DB : ViewDataBinding> : BottomSheetDialogFragment() {
    open val bottomSheetData: EWalletBottomSheetData = EWalletBottomSheetData()
    lateinit var binding: DB

    @LayoutRes
    abstract fun inflateLayout(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                val view: View? = currentFocus
                val ret = super.dispatchTouchEvent(ev)
                if (view is EditText) {
                    currentFocus?.let {
                        val w: View = it
                        val scrcoords = IntArray(2)
                        w.getLocationOnScreen(scrcoords)
                        val x: Float = ev.rawX + w.left - scrcoords[0]
                        val y: Float = ev.rawY + w.top - scrcoords[1]
                        if (ev.action == MotionEvent.ACTION_UP
                            && (x < w.left || x >= w.right || y < w.top || y > w.bottom)
                        ) {
                            view.let {
                                val inputMethodManager = requireContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                                ) as InputMethodManager
                                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                            }
                        }
                    }
                }
                return ret
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            inflateLayout(),
            container,
            false
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "namescreen-bottomsheet: ${this::class.java.simpleName} ")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val bottomSheetBehavior = dialog.behavior
            bottomSheetBehavior.isDraggable = bottomSheetData.isDraggable
            isCancelable = bottomSheetData.isCancelable
            // set height
            when (val type = bottomSheetData.heightBottomSheetType) {
                is HeightBottomSheetType.CustomHeight -> {
                    bottomSheet?.let { sheet ->
                        val displayMetrics = Resources.getSystem().displayMetrics
                        val screenHeight = displayMetrics.heightPixels
                        val desiredHeight =
                            (screenHeight * type.heightBottomSheetPercent).toInt()
                        val layoutParams = sheet.layoutParams
                        layoutParams.height = desiredHeight
                        sheet.requestLayout()
                        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                HeightBottomSheetType.WrapContent -> {
                    dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                // should create view with LinearLayout
                is HeightBottomSheetType.WrapContentWithScrollView -> {
                    (bottomSheet?.parent as? View)?.let { parentView ->
                        var oldHeightBottomSheet = parentView.height

                        parentView.viewTreeObserver?.addOnGlobalLayoutListener {
                            val newHeight = parentView.height
                            if (oldHeightBottomSheet == newHeight) return@addOnGlobalLayoutListener

                            oldHeightBottomSheet = newHeight
                            val scrollViewChildHeight = (binding.root as? ViewGroup)?.let { rootView ->
                                newHeight - rootView.children.filter {it !is MaxHeightScrollView }.sumOf { childView ->
                                    childView.measure(
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                                    )
                                    childView.measuredHeight
                                }
                            } ?: newHeight
                            if(scrollViewChildHeight> 0){
                                type.scrollView.setMaxHeight(scrollViewChildHeight)
                                dialog.currentFocus?.let{
                                    // fix bug scrollview not scroll to edt at the first focus after dialog open
                                    type.scrollView.postDelayed({
                                        it.parent.requestChildFocus(it, it)
                                    }, 100)
                                }
                            }
                        }
                    }
                }
            }

            // fix bug dialog not show full content
            bottomSheet?.let { sheet ->
                dialog.behavior.peekHeight = sheet.height
                sheet.parent.parent.requestLayout()
            }
        }
    }

    /**
     * show bottom sheet
     *
     * @param fragmentManager FragmentManager
     */
    fun showDialog(fragmentManager: FragmentManager) {
        if (!isShowingDialog(fragmentManager, this::class.java.simpleName)) {
            show(fragmentManager, this::class.java.simpleName)
        }
    }
}

fun isShowingDialog(manager: FragmentManager?, tag: String?): Boolean {
    val dialogFragment = manager?.findFragmentByTag(tag ?: "")

    dialogFragment?.let {
        if (it is DialogFragment) {
            it.dialog?.let {
                return true
            }
        }
    }
    return false
}