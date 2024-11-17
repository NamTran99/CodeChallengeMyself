package com.example.myapplication.ui.screen.dialog

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseBottomSheetDialog
import com.example.myapplication.databinding.DialogTestBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TestBottomSheetDialog: BaseBottomSheetDialog<DialogTestBinding>() {
    override val layoutId: Int = R.layout.dialog_test

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect) // Lấy kích thước hiển thị hiện tại

            val screenHeight = binding.root.rootView.height // Chiều cao toàn màn hình
            val visibleHeight = rect.bottom // Chiều cao hiển thị còn lại
            val keyboardHeight = screenHeight - visibleHeight // Chiều cao bàn phím

            if (keyboardHeight > screenHeight * 0.15) { // Kiểm tra nếu chiều cao đủ lớn (khoảng 15% màn hình)
                // Bàn phím đang hiển thị
                Log.d("KeyboardHeight", "Keyboard Height: $keyboardHeight")

                (binding.root.parent as View) .apply {
                    layoutParams.height = 400
                    requestLayout()
                }
            } else {
                // Bàn phím đã đóng
                Log.d("KeyboardHeight", "Keyboard is closed")
            }

        }
    }
}