package com.example.myapplication.ui.screen.dialog

import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseBottomSheetDialog
import com.example.myapplication.databinding.DialogTestBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TestBottomSheetDialog: BaseBottomSheetDialog<DialogTestBinding>() {
    override val layoutId: Int = R.layout.dialog_test

}