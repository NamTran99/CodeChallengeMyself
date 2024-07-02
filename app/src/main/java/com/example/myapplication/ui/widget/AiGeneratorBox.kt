package com.example.myapplication.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.R
import com.example.myapplication.databinding.AiGeneratorBoxBinding
import com.example.myapplication.extensions.fadeInFromRight
import com.example.myapplication.extensions.hide
import com.example.myapplication.extensions.show
import com.example.myapplication.extensions.toggleImage


sealed interface AIGeneratorStatus {
    object None : AIGeneratorStatus
    object Ready : AIGeneratorStatus
    object Loading : AIGeneratorStatus
    data class Success(val content: String, val isExpand: Boolean = true) : AIGeneratorStatus
    data class Error(val content: String) : AIGeneratorStatus
}

interface IOnAIGeneratorCallBack{
    fun onCallData()
}

class AiGeneratorBox(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    val binding =
        AiGeneratorBoxBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    private val listViewExpand = listOf(binding.tvContent, binding.grAction)

    @Volatile
    private var type: AIGeneratorStatus = AIGeneratorStatus.None
        set(value) {
            val isUpdateUI = value::class.java != field::class.java
            field = value
            if(!isUpdateUI) return
            binding.apply {
                imgBot.show(value != AIGeneratorStatus.Loading)
                imgLoading.show(value == AIGeneratorStatus.Loading)
                tvContent.hide(value == AIGeneratorStatus.Loading || value == AIGeneratorStatus.Ready)
                grAction.hide(value is AIGeneratorStatus.Success)
                imgExpand.show(value is AIGeneratorStatus.Success)
                when (value) {
                    AIGeneratorStatus.Ready -> {
                        setTitle(R.string.ai_bot_title_ready)
                        tvContent.setTextColor(Color.BLACK)
                        grAction.hide()
                    }

                    AIGeneratorStatus.Loading -> {
                        setTitle(R.string.ai_bot_title)
                        tvContent.setTextColor(Color.BLACK)
                        grAction.hide()
                    }

                    is AIGeneratorStatus.Success -> {
                        setTitle(R.string.ai_bot_title)
                        tvContent.animateText(value.content) {
                            grAction.show()
                            grAction.fadeInFromRight()
                        }
                        tvContent.setTextColor(Color.BLACK)

                    }

                    is AIGeneratorStatus.Error -> {
                        setTitle(R.string.ai_bot_title)
                        tvContent.animateText(value.content)
                        tvContent.setTextColor(Color.RED)
                        grAction.hide()
                    }

                    AIGeneratorStatus.None -> Unit
                }
            }
        }

    init {
        initView()
    }

    private fun initView() {
        binding.apply {
            root.setOnClickListener {
                when (type) {
                    AIGeneratorStatus.Ready ->{
                        callBack?.onCallData()
                        setStatus(AIGeneratorStatus.Loading)
                    }
                    is AIGeneratorStatus.Success -> {
                        val temp = type as? AIGeneratorStatus.Success ?: return@setOnClickListener
                        if(tvContent.isDoneAnimate){
                            type = temp.copy(isExpand = !temp.isExpand)
                            imgExpand.toggleImage(temp.isExpand, R.drawable.ic_arrow_down, R.drawable.ic_arrow_up)
                            listViewExpand.forEach {
                                it.show(!temp.isExpand){
                                    it.fadeInFromRight()
                                }
                            }
                        }else{
                            tvContent.forceShowFullContent()
                        }

                    }
                    else -> Unit
                }
            }

            imgHeart.toggleImage(false, R.drawable.ic_heart_less, R.drawable.ic_heart_full) {

            }

            type = AIGeneratorStatus.Ready
        }
    }

    fun setStatus(data: AIGeneratorStatus) {
        type = data
    }

    private var callBack: IOnAIGeneratorCallBack? = null
    fun setCallBack(callback: IOnAIGeneratorCallBack){
        this.callBack = callback
    }

    private fun setTitle(@StringRes title: Int) {
        binding.tvTitle.setText(title)
    }
}