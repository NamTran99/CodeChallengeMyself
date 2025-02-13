package com.example.mybase.core.platform

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mybase.R

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {
    abstract val layoutID: Int

    data class ActivityConfigData(
        val hideKeyboardWhenClickOutSideEditText: Boolean = false
    )

    lateinit var binding: T
    protected lateinit var navController: NavController

    open val activityConfig: ActivityConfigData = ActivityConfigData()
    open val navHostFragmentID: Int? = null


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && activityConfig.hideKeyboardWhenClickOutSideEditText) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, layoutID, null, false)
        setContentView(binding.root)
        navHostFragmentID?.let{
            val navHostFragment =
                supportFragmentManager.findFragmentById(it) as NavHostFragment
            navController = navHostFragment.navController
        }
    }

    override fun onResume() {
        Log.d("TAG", "namescreen-activity: NamTD8 ${this::class.simpleName}")
        super.onResume()
    }
}