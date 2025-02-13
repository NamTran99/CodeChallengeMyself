package com.example.mybase.extensions

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun CoroutineScope.launchWithSupervisorJob(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return this.launch(SupervisorJob() + dispatcher) {
        block()
    }
}

inline fun <reified T> String.fromJson(): T? {
    return try {
        Gson().fromJson(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}

val scopeSupervisorIO = CoroutineScope(Dispatchers.IO + SupervisorJob())
val scopeSupervisorMain = CoroutineScope(Dispatchers.Main + SupervisorJob())

