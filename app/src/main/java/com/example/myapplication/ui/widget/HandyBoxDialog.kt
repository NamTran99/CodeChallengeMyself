package com.example.myapplication.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.HandyDialogCreateAccountBinding
import com.example.mybase.extensions.setOnSafeClickListener
import com.example.mybase.extensions.show


enum class HandyBoxStatus{
    Welcome, Loading, Success, Error
}

class HandyBoxData(
    val status: HandyBoxStatus = HandyBoxStatus.Welcome,
    val errorContent: Int?= null
)

class HandyBoxDialog(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    val binding =
        HandyDialogCreateAccountBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    private var mStatusView : HandyBoxData =HandyBoxData()
        set(value) {
            setUpUI(value)
            field = value
        }

    private var isShowSuccessConfigContent = false
        set(value) {
            binding.apply {
                viewLvSuccessContent.show(value)
            }
            field = value
        }

    init {
//        context.loadAttrs(attributeSet, R.styleable.AnimArrowView) {
//        }

        initView()
    }

    private fun initView() {
        binding.apply {
            lvConfigSuccess.setOnSafeClickListener {
                isShowSuccessConfigContent = isShowSuccessConfigContent.not()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private fun setUpUI(data:HandyBoxData){
        binding.apply {
            lvAnimateLayout.show(data.status != HandyBoxStatus.Success)
            lvConfigSuccess.show(data.status == HandyBoxStatus.Success)
            if(data.status != HandyBoxStatus.Error){
                lvAnimateLayout.setBackgroundResource(R.drawable.bg_corner_10_white)
            }

            var animation = R.raw.anim_handy_box_welcome
            var content = R.string.txt_handy_box_welcome

            when(data.status){
                HandyBoxStatus.Success -> {
                    animation = R.raw.anim_handy_box_welcome
                    content = R.string.txt_handy_box_welcome
                }
                HandyBoxStatus.Loading ->{
                    animation = R.raw.anim_loading
                    content = R.string.txt_handy_box_loading
                }
                HandyBoxStatus.Error->{
                    lvAnimateLayout.setBackgroundResource(R.drawable.bg_corner_10_stroke)
                    lvAnimateLayout.background.setTint(context.resources.getColor(R.color.red, null))
                    isShowSuccessConfigContent = false
                    animation = R.raw.anim_handy_box_error
                    content = data.errorContent?: return
                }
                else ->{

                }
            }
            animBox.setAnimation(animation)
            animBox.playAnimation()
            txtContent.setText(content)
        }
    }
    fun setData(data: HandyBoxData){
        mStatusView = data
    }

}