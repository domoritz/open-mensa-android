package de.uni_potsdam.hpi.openmensa.helpers

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

object HighPriorityDispatcher: CoroutineDispatcher() {
    private val handler = Handler(Looper.getMainLooper())

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.postAtFrontOfQueue(block)
    }
}