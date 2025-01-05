package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
//import umagic.ai.aiart.retrofit.TokenUtils
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_main, null, false)
        setContentView(binding.root)

    }

    override fun onResume() {
        super.onResume()
        val supportedAbis = Build.SUPPORTED_ABIS
        val primaryAbi = supportedAbis.firstOrNull() ?: "Không xác định"

        Log.d("TAG","Các ABI được hỗ trợ: ${supportedAbis.joinToString()}")
        Log.d("TAG","Kiến trúc chính: $primaryAbi")

        val a = UUID.randomUUID().toString()
//        TokenUtils.initLib()
//        val b = TokenUtils.tokenUtilsInstance?.paramsToken(a)
//        Log.d("TAG", "onResume: NamTD8 ${a} - ${b}")
    }

    // unfocus && close keyboard edittext when select outside
//    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_UP) {
//            val v = currentFocus
//            if (v is EditText) {
//                val outRect = Rect()
//                v.getGlobalVisibleRect(outRect)
//                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
//                    v.clearFocus()
//                    val imm: InputMethodManager =
//                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
//                }
//            }
//        }
//        return super.dispatchTouchEvent(event)
//    }

}
