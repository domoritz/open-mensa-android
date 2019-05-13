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
    private val statusInternal = mutableMapOf<Int, MealSyncingStatus>()
    private val statusInternalLive = MutableLiveData<Map<Int, MealSyncingStatus>>().apply { postValue(statusInternal.toMap()) }
    val status: LiveData<Map<Int, MealSyncingStatus>> = statusInternalLive

    fun syncInBackground(canteenId: Int, force: Boolean, context: Context) {
        Threads.network.execute {
            try {
                syncCanteenSynchronousThrowEventually(canteenId, force, context)
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "meal syncing failed", ex)
                }
            }
        }
    }

    private fun syncCanteenSynchronousThrowEventually(canteenId: Int, force: Boolean, context: Context) {
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

        if (reportCanteenSyncing(canteenId)) {
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

                setCanteenStatus(canteenId, MealSyncingDone)
            } catch (ex: Exception) {
                setCanteenStatus(canteenId, MealSyncingFailed)

                throw ex
            }
        }
    }

    private fun reportCanteenSyncing(canteenId: Int): Boolean {
        synchronized(lock) {
            return if (statusInternal[canteenId] == MealSyncingRunning) {
                false
            } else {
                statusInternal[canteenId] = MealSyncingRunning
                updateLiveData()

                true
            }
        }
    }

    private fun setCanteenStatus(canteenId: Int, status: MealSyncingStatus) {
        synchronized(lock) {
            statusInternal[canteenId] = status
            updateLiveData()
        }
    }

    fun removeDoneStatus(canteenId: Int) {
        synchronized(lock) {
            val currentStatus = statusInternal[canteenId]

            if (currentStatus == MealSyncingFailed || currentStatus == MealSyncingDone) {
                statusInternal.remove(canteenId)
                updateLiveData()
            }
        }
    }

    private fun updateLiveData() {
        statusInternalLive.postValue(statusInternal.toMap())
    }
}

sealed class MealSyncingStatus
object MealSyncingRunning: MealSyncingStatus()
object MealSyncingFailed: MealSyncingStatus()
object MealSyncingDone: MealSyncingStatus()