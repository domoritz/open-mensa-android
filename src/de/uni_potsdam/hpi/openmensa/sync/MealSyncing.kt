package de.uni_potsdam.hpi.openmensa.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.Threads
import de.uni_potsdam.hpi.openmensa.api.client.HttpApiClient
import de.uni_potsdam.hpi.openmensa.data.AppDatabase

// TODO: report errors in the UI
object MealSyncing {
    private const val LOG_TAG = "MealSyncing"
    private val lock = Object()
    private val refreshedCanteenIdsInternal = mutableSetOf<Int>()
    private val refreshedCanteenIdsInternalLive = MutableLiveData<Set<Int>>().apply { postValue(refreshedCanteenIdsInternal.toSet()) }
    val refreshedCanteenIds: LiveData<Set<Int>> = refreshedCanteenIdsInternalLive

    fun syncInBackground(canteenId: Int, force: Boolean, context: Context) {
        Threads.network.execute {
            try {
                syncCanteenSynchronousThrowEventually(canteenId, false, context)
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "meal syncing failed", ex)
                }
            }
        }
    }

    fun syncCanteenSynchronousThrowEventually(canteenId: Int, force: Boolean, context: Context) {
        val database = AppDatabase.with(context)

        fun shouldSync() = force || database.lastCanteenSync().getByCanteenIdSync(canteenId)?.let {
            val now = System.currentTimeMillis()

            it.timestamp > now || it.timestamp + 1000 * 60 * 60 /* 1 hour */ < now
        } ?: true

        if (!shouldSync()) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "should not sync now")
            }

            return
        }

        if (addCanteenToLiveData(canteenId)) {
            try {
                if (!shouldSync()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "should not sync now")
                    }

                    return
                }

                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "sync $canteenId now")
                }

                SyncUtil.syncDaysAndMeals(
                        api = HttpApiClient.getInstance(context),
                        database = database,
                        canteenId = canteenId
                )
            } finally {
                removeCanteenFromLiveData(canteenId)
            }
        }
    }

    private fun addCanteenToLiveData(canteenId: Int): Boolean {
        synchronized(lock) {
            return if (refreshedCanteenIdsInternal.add(canteenId)) {
                refreshedCanteenIdsInternalLive.postValue(refreshedCanteenIdsInternal.toSet())
                true
            } else false
        }
    }

    private fun removeCanteenFromLiveData(canteenId: Int): Boolean {
        synchronized(lock) {
            return if (refreshedCanteenIdsInternal.remove(canteenId)) {
                refreshedCanteenIdsInternalLive.postValue(refreshedCanteenIdsInternal.toSet())
                true
            } else false
        }
    }
}