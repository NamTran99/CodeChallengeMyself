package com.example.myapplication.ui.screen

import android.os.Bundle
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.core.platform.BaseFragment
import com.example.myapplication.databinding.FragmentEditTextExtensionBinding
import com.example.myapplication.extensions.formatInputToDecimalPlaces
import java.lang.reflect.Field

class FragmentEditTextExtension : BaseFragment<FragmentEditTextExtensionBinding>(){
    override val layoutId: Int
        get() = R.layout.fragment_edit_text_extension

    private fun removeAllListener() {
        try {
            val field: Field? = findField("mListeners", binding.edtTest.javaClass )
            field?.let{
                it.isAccessible = true
                it.get(binding.edtTest)
                (it.get( binding.edtTest) as? ArrayList<*>)?.clear()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findField(name: String, type: Class<*>): Field? {
        for (declaredField in type.declaredFields) {
            if (declaredField.getName() == name) {
                return declaredField
            }
        }
        return if (type.superclass != null) {
            findField(name, type.superclass)
        } else null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            edtTest.formatInputToDecimalPlaces("3",9)
            removeAllListener()
            edtTest.formatInputToDecimalPlaces("2",9)
        }
    }
}