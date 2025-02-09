//package com.tdt.pmobile3.ewallet.base.recyclerview.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.annotation.LayoutRes
//import androidx.databinding.DataBindingUtil
//import androidx.databinding.ViewDataBinding
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.tdt.pmobile3.databinding.ItemEwalletLoadMoreVerticalBinding
//import com.tdt.pmobile3.ewallet.base.recyclerview.core.DataBindingViewHolder
//import com.tdt.pmobile3.ewallet.base.recyclerview.core.KeyCallbackItem
//import com.tdt.pmobile3.ewallet.base.recyclerview.core.KeyModel
//import com.tdt.pmobile3.ewallet.base.recyclerview.helper.BaseLoadMoreRecyclerOnScrollListener
//
//abstract class LoadMoreSingleHolderBindingAdapter<T : KeyModel, B : ViewDataBinding>(
//    private val recycler: RecyclerView,
//    private val loadMoreRecyclerOnScrollListener: BaseLoadMoreRecyclerOnScrollListener
//) : ListAdapter<T, RecyclerView.ViewHolder>(KeyCallbackItem<T>()) {
//
//
//    companion object {
//        private const val VIEW_TYPE_ITEM = 0
//        private const val VIEW_TYPE_LOADING = 1
//    }
//
//    init {
//        recycler.addOnScrollListener(loadMoreRecyclerOnScrollListener.apply {
//            onLoadMore = { page ->
//                showLoading(true)
//                onLoadMoreListener?.invoke(page)
//            }
//        })
//    }
//
//    private var isLoading = false
//
//    @get:LayoutRes
//    protected abstract val layoutId: Int
//
//    var onLoadMoreListener: ((Int) -> Unit)? = null
//
//    override fun getItemViewType(position: Int): Int {
//        return if (position == itemCount - 1 && isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int,
//    ): RecyclerView.ViewHolder {
//        return when (viewType) {
//            VIEW_TYPE_LOADING -> LoadingViewHolder.from(parent)
//            else -> {
//                val holder = DataBindingViewHolder.from<T, B>(
//                    DataBindingUtil.inflate(
//                        LayoutInflater.from(parent.context),
//                        layoutId,
//                        parent,
//                        false
//                    )
//                )
//                initStateViewHolder(holder)
//                holder
//            }
//        }
//    }
//
//    override fun onBindViewHolder(
//        holder: RecyclerView.ViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        if (holder is DataBindingViewHolder<*, *>) {
//            val binding = holder.binding as? B
//            if (binding != null && !onBindHandlePayload(
//                    binding,
//                    getItem(position),
//                    position,
//                    payloads
//                )
//            ) {
//                super.onBindViewHolder(holder, position, payloads)
//            }
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataBindingViewHolder<*, *>) {
//            (holder.binding as? B)?.apply {
//                onBind(this, getItem(position), position)
//            }
//        }
//    }
//
//    abstract fun onBind(binding: B, item: T, position: Int)
//
//    open fun onBindHandlePayload(
//        binding: B,
//        model: T,
//        position: Int,
//        payloads: MutableList<Any>
//    ): Boolean {
//        return false
//    }
//
//    override fun getItemCount(): Int {
//        return super.getItemCount() + if (isLoading) 1 else 0
//    }
//
//    open fun initStateViewHolder(holder: DataBindingViewHolder<T, B>) {
//        // initial State ViewHolder
//    }
//
//    fun showLoading(isLoading: Boolean) {
//        if (this.isLoading == isLoading) return
//        this.isLoading = isLoading
//
//        if (isLoading) {
//            notifyItemInserted(itemCount - 1)
//        } else  {
//            notifyItemRemoved(itemCount)
//        }
//    }
//    fun submitAddList(list: List<T>) {
//        val newList = currentList.toMutableList()
//        newList.addAll(list)
//        submitList(newList)
//        loadMoreRecyclerOnScrollListener.setLoadingFinished()
//        showLoading(false)
//    }
//
//    fun submitList(list: List<T>?, pageNum: Int,isEnableLoadMore :Boolean) {
//        showLoading(false)
//        super.submitList(list){
//            loadMoreRecyclerOnScrollListener.currentPage = pageNum
//            loadMoreRecyclerOnScrollListener.setLoadingFinished()
//            loadMoreRecyclerOnScrollListener.isLastPage = !isEnableLoadMore
//        }
//    }
//}
//
//class LoadingViewHolder(binding: ItemEwalletLoadMoreVerticalBinding) :
//    RecyclerView.ViewHolder(binding.root) {
//    companion object {
//        fun from(parent: ViewGroup): LoadingViewHolder {
//            val inflater = LayoutInflater.from(parent.context)
//            val binding = ItemEwalletLoadMoreVerticalBinding.inflate(inflater, parent, false)
//            return LoadingViewHolder(binding)
//        }
//    }
//}
