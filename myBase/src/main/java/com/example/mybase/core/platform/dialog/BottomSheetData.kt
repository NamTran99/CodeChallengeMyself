package com.example.mybase.core.platform.dialog

import com.example.mybase.ui.widget.MaxHeightScrollView

data class EWalletBottomSheetData(
    val isDraggable: Boolean = true,
    val isCancelable: Boolean = true,
    val heightBottomSheetType: HeightBottomSheetType = HeightBottomSheetType.WrapContent
)

sealed class HeightBottomSheetType {
    data object WrapContent : HeightBottomSheetType()
    data class WrapContentWithScrollView(val scrollView: MaxHeightScrollView) : HeightBottomSheetType()
    data class CustomHeight(val heightBottomSheetPercent: Float) : HeightBottomSheetType()  // range 0..1
}