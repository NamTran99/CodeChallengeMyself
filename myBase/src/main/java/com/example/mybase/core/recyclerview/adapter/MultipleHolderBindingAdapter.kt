//package com.tdt.pmobile3.ewallet.base.recyclerview.adapter
//
//import android.view.ViewGroup
//import androidx.annotation.LayoutRes
//import androidx.databinding.ViewDataBinding
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.tdt.pmobile3.ewallet.base.recyclerview.core.KeyCallbackItem
//import com.tdt.pmobile3.ewallet.base.recyclerview.core.KeyModel
//import com.tdt.pmobile3.ewallet.utils.inflateBinding
//import kotlin.reflect.KClass
//
//
//open class MultipleViewHolderAdapter(
//    private val holderList: ViewHolderList,
//) : ListAdapter<KeyModel, TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder>(
//    KeyCallbackItem<KeyModel>()
//) {
//    override fun getItemViewType(position: Int): Int {
//        return holderList.getViewHolderTypeWithItem(getItem(position))
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int,
//    ): TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder {
//        return holderList.getViewHolderByType(viewType, parent)
//    }
//
//    override fun onBindViewHolder(
//        holder: TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder,
//        position: Int,
//    ) {
//        holder.bind(getItem(position), position)
//    }
//
//
//    override fun onBindViewHolder(
//        holder: TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        if(holder.bind(getItem(position), position, payloads).not()){
//            super.onBindViewHolder(holder, position, payloads)
//        }
//    }
//}
//
//class ViewHolderList(
//    val typeList: List<TypeItemHolder<out KeyModel, out ViewDataBinding>>,
//) {
//
//     inline fun <reified TYPE: TypeItemHolder<*, *>> getViewHolderType(): TYPE? {
//        return typeList.find { it is TYPE } as? TYPE
//    }
//
//    fun getViewHolderTypeWithItem(item: KeyModel): Int {
//        typeList.forEachIndexed { index, typeViewHolder ->
//            if (typeViewHolder.checkModel(item)) {
//                return index
//            }
//        }
//        return 0
//    }
//
//    fun getViewHolderByType(
//        type: Int,
//        parent: ViewGroup,
//    ): TypeItemHolder<out KeyModel, out ViewDataBinding>.DataViewHolder {
//        return typeList[type].getViewHolder(parent)
//    }
//
//}
//
//abstract class TypeItemHolder<MODEL : KeyModel, BINDING : ViewDataBinding>(
//    private val model: KClass<out KeyModel>,
//    @LayoutRes val layoutRes: Int,
//) {
//    abstract fun onBindView(binding: BINDING, model: MODEL, position: Int)
//    open fun onBindHandlePayload(binding: BINDING, model: MODEL, position: Int, payloads: MutableList<Any>): Boolean{
//        return false
//    }
//
//    fun <MODEL : KeyModel> checkModel(item: MODEL): Boolean {
//        return model.java.isAssignableFrom(item::class.java)
//    }
//
//    fun getViewHolder(parent: ViewGroup): DataViewHolder {
//        return DataViewHolder(parent.inflateBinding(layoutRes))
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    inner class DataViewHolder(
//        val binding: BINDING,
//    ) : RecyclerView.ViewHolder(binding.root) {
//         fun bind(item: KeyModel, position: Int) {
//            onBindView(binding, item as MODEL, position)
//        }
//
//        fun bind(item: KeyModel, position: Int, payloads: MutableList<Any>): Boolean {
//            return onBindHandlePayload(binding, item as MODEL, position, payloads)
//        }
//    }
//}