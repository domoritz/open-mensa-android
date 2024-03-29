package de.uni_potsdam.hpi.openmensa.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.Threads
import de.uni_potsdam.hpi.openmensa.api.client.HttpApiClient
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget

object CanteenSyncing {
    private const val LOG_TAG = "CanteenSyncing"
    private val lock = Object()
    private val isWorkingInternal = MutableLiveData<Boolean>().apply { value = false }
    val isWorking: LiveData<Boolean> = isWorkingInternal

    fun runBackgroundSync(context: Context) {
        Threads.network.execute {
            try {
                runSynchronousAndThrowEventually(force = false, context = context)
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "background canteen syncing failed", ex)
                }
            }
        }
    }

    private fun shouldDoBackgroundSync(context: Context): Boolean {
        val lastSync = SettingsUtils.with(context).lastCanteenListUpdate

        return lastSync + 1000 * 60 * 60 * 24 * 7 /* 7 days */ < System.currentTimeMillis()
    }

    fun runSynchronousAndThrowEventually(force: Boolean, context: Context) {
        if ((!force) && (!shouldDoBackgroundSync(context))) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "skip background sync")
            }

            return
        }

        synchronized(lock) {
            if ((!force) && (!shouldDoBackgroundSync(context))) {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "skip background sync")
                }

                return
            }

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "doing sync; force = $force")
            }

            try {
                isWorkingInternal.postValue(true)

                SyncUtil.syncCanteenList(
                        api = HttpApiClient.getInstance(context),
                        database = AppDatabase.with(context)
                )

                MealWidget.updateAppWidgets(context)

                SettingsUtils.with(context).lastCanteenListUpdate = System.currentTimeMillis()
            } finally {
                isWorkingInternal.postValue(false)
            }
        }
    }
}