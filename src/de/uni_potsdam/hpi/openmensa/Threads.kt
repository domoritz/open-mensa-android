package de.uni_potsdam.hpi.openmensa

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object Threads {
    val network: Executor = Executors.newFixedThreadPool(4)
    val database: Executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())
}