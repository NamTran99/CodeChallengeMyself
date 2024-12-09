package com.example.myapplication.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

inline fun <T1, T2, R> LiveData<T1>.zip(
    liveData2: LiveData<T2>,
    crossinline transform: (T1, T2) -> R
): LiveData<R> = zipLiveData(this, liveData2) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2
    )
}

inline fun <R> zipLiveData(
    vararg varargLiveData: LiveData<*>,
    crossinline transform: (Array<*>) -> R
) = MediatorLiveData<R>().apply {
    val hashSetIndex = HashSet<Int>()
    varargLiveData.forEachIndexed {index,  liveData ->
        addSource(liveData) {
            hashSetIndex.add(index)
            if(hashSetIndex.size == varargLiveData.size){
                val listDataCallback = varargLiveData.map {
                    it.value ?: return@addSource
                }.toTypedArray()
                value = transform(listDataCallback)
                hashSetIndex.clear()
            }
        }
    }
}

inline fun <T1, T2, R> LiveData<T1>.combine(
    liveData2: LiveData<T2>,
    crossinline transform: (T1?, T2?) -> R
): LiveData<R> = combineLiveData(this, liveData2) {
        args: Array<*> ->
    transform(
        args[0] as T1?,
        args[1] as T2?
    )
}

inline fun <R> combineLiveData(
    vararg varargLiveData: LiveData<*>,
    crossinline transform: (Array<*>) -> R
) = MediatorLiveData<R>().apply {
    varargLiveData.forEachIndexed { index, liveData ->
        addSource(liveData) {
            val listDataCallback = varargLiveData.map {
                it.value
            }.toTypedArray()
            value = transform(listDataCallback)
        }
    }
}

