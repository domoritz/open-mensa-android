package de.uni_potsdam.hpi.openmensa.sync

import android.content.Context
import android.util.Log
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.api.client.HttpApiClient
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CanteenSyncing {
    private const val LOG_TAG = "CanteenSyncing"
    private val lock = Object()

    private fun shouldDoBackgroundSync(context: Context): Boolean {
        val lastSync = SettingsUtils.with(context).lastCanteenListUpdate

        return lastSync + 1000 * 60 * 60 * 24 * 7 /* 7 days */ < System.currentTimeMillis()
    }

    suspend fun runSynchronousAndThrowEventually(force: Boolean, context: Context) = withContext(Dispatchers.IO) {
        if ((!force) && (!shouldDoBackgroundSync(context))) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "skip background sync")
            }

            return@withContext
        }

        synchronized(lock) {
            if ((!force) && (!shouldDoBackgroundSync(context))) {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "skip background sync")
                }

                return@withContext
            }

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "doing sync; force = $force")
            }

            SyncUtil.syncCanteenList(
                api = HttpApiClient.getInstance(context),
                database = AppDatabase.with(context)
            )

            MealWidget.updateAppWidgets(context)

            SettingsUtils.with(context).lastCanteenListUpdate = System.currentTimeMillis()
        }
    }
}