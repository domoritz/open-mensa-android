package de.uni_potsdam.hpi.openmensa.sync

import android.content.Context
import android.util.Log
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.api.client.HttpApiClient
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object MealSyncing {
    private const val LOG_TAG = "MealSyncing"
    private val lock = Object()
    private val mutexes = mutableMapOf<Int, Mutex>()

    suspend fun shouldSync(canteenId: Int, context: Context): Boolean = withContext(Dispatchers.IO) {
        val database = AppDatabase.with(context)

        database.lastCanteenSync.getByCanteenIdSync(canteenId)?.let {
            val now = System.currentTimeMillis()

            it.timestamp > now || it.timestamp + 1000 * 60 * 60 /* 1 hour */ < now
        } ?: true
    }

    suspend fun syncCanteenSynchronousThrowEventually(canteenId: Int, force: Boolean, context: Context) = withContext(Dispatchers.IO) {
        getMutex(canteenId).withLock {
            val database = AppDatabase.with(context)

            if (!force && !shouldSync(canteenId, context)) {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "should not sync now")
                }

                return@withContext
            }

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "sync $canteenId now")
            }

            SyncUtil.syncDaysAndMeals(
                api = HttpApiClient.getInstance(context),
                database = database,
                canteenId = canteenId
            )

            MealWidget.updateAppWidgets(
                context = context,
                appWidgetIds = database.widgetConfiguration.getWidgetIdsByCanteenId(canteenId)
            )
        }
    }

    private fun getMutex(canteenId: Int): Mutex = synchronized(lock) {
        mutexes.getOrPut(canteenId) { Mutex() }
    }
}