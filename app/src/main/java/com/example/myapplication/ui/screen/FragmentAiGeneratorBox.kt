package com.example.myapplication.ui.screen

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentAiGeneratorBoxBinding
import com.example.myapplication.ui.widget.AIGeneratorStatus
import com.example.myapplication.ui.widget.IOnAIGeneratorCallBack
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class FragmentAiGeneratorBox : BaseFragment<FragmentAiGeneratorBoxBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_ai_generator_box

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with most use cases
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = BuildConfig.API_AI_KEY
        )

        val prompt = "Write a story about a magic backpack."

        binding.apply {
            boxAi.setCallBack(
                object : IOnAIGeneratorCallBack{
                    override fun onCallData() {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val response = generativeModel.generateContent(prompt)
                            boxAi.setStatus(AIGeneratorStatus.Success(content = response.text?: "loi nhe"))
                        }
                    }
                }
            )
        }
    }


    fun callData(){

    }
}