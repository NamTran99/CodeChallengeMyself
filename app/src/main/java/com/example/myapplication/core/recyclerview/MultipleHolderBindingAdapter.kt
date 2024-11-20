package com.example.myapplication.core.recyclerview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.extensions.inflateBinding
import kotlin.reflect.KClass


open class MultipleViewHolderAdapter(
    private val holderList: ViewHolderList,
) : ListAdapter<KeyModel, TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder>(
    KeyCallbackItem<KeyModel>()
) {

    override fun getItemViewType(position: Int): Int {
        return holderList.getViewHolderTypeWithItem(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder {
        return holderList.getViewHolderByType(viewType, parent)
    }

    override fun onBindViewHolder(
        holder: TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position), position)
    }
}

class ViewHolderList(
    private val typeList: List<TypeItemHolder<out KeyModel, out ViewDataBinding>>,
) {
    fun getViewHolderTypeWithItem(item: KeyModel): Int {
        typeList.forEachIndexed { index, typeViewHolder ->
            if (typeViewHolder.checkModel(item)) {
                return index
            }
        }
        return 0
    }

    fun getViewHolderByType(
        type: Int,
        parent: ViewGroup,
    ): TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder {
        return typeList[type].getViewHolder(parent)
    }

}

abstract class TypeItemHolder<MODEL : KeyModel, BINDING : ViewDataBinding>(
    private val model: KClass<out KeyModel>,
    @LayoutRes val layoutRes: Int,
) {

    abstract fun onBindViewModel(binding: BINDING, model: MODEL, position: Int)

    fun <MODEL : KeyModel> checkModel(item: MODEL): Boolean {
        return model.java.isAssignableFrom(item::class.java)
    }

    fun getViewHolder(parent: ViewGroup): DataViewHolder {
        return DataViewHolder(parent.inflateBinding(layoutRes))
    }

    @Suppress("UNCHECKED_CAST")
    inner class DataViewHolder(
        private val binding: BINDING,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: KeyModel, position: Int) {
            onBindViewModel(binding, item as MODEL, position)
        }
    }
}