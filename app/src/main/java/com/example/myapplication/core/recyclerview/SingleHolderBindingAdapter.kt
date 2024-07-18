package com.example.myapplication.core.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.ParameterizedType

abstract class SingleHolderBindingAdapter<T : KeyModel, B : ViewDataBinding>(

) : ListAdapter<T, DataBindingViewHolder<T, B>>(KeyCallbackItem<T>()) {
    @get:LayoutRes
    protected abstract val layoutId: Int
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DataBindingViewHolder<T, B> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val holder = DataBindingViewHolder.from<T, B>(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false)
        )
        initStateViewHolder(holder)
        return holder
    }


    override fun onBindViewHolder(holder: DataBindingViewHolder<T, B>, position: Int) {
        onBind(holder.binding, getItem(position), position)
    }

    abstract fun onBind(binding: B, item: T, position: Int)

    open fun initStateViewHolder(holder: DataBindingViewHolder<T, B>) {}
}

abstract class AbstractDataBindingViewHolder<out T : ViewDataBinding>(
    val binding: T,
) : RecyclerView.ViewHolder(binding.root) {
}


@Suppress("UNCHECKED_CAST")
fun <VB : ViewDataBinding> getViewHolderBinding(
    clazz: Class<*>,
    inflater: LayoutInflater,
    container: ViewGroup?,
): VB? {
    return try {
        (clazz.genericSuperclass as? ParameterizedType)
            ?.actualTypeArguments
            ?.getOrNull(1)
            ?.let {
                (it as Class<*>).getMethod(
                    "inflate",
                    LayoutInflater::class.java,
                    ViewGroup::class.java,
                    Boolean::class.java
                ).let { method ->
                    method.invoke(null, inflater, container, false) as VB
                }
            }
    } catch (e: Exception) {
        null
    }
}