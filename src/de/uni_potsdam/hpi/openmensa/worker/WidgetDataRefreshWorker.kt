package de.uni_potsdam.hpi.openmensa.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.TimeUnit

class WidgetDataRefreshWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        private const val LOG_TAG = "WidgetDataRefreshWorker"
        private const val WORK_NAME = "WidgetDataRefreshWorker"

        fun update(context: Context) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "update()")
            }

            val appWidgetIds = MealWidget.getAppWidgetIds(context)

            if (appWidgetIds.isNotEmpty()) schedule(context)
            else disable(context)
        }

        fun schedule(context: Context) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "schedule()")
            }

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(1, TimeUnit.DAYS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )
        }

        fun disable(context: Context) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "disable()")
            }

            WorkManager.getInstance(context).apply {
                cancelUniqueWork(WORK_NAME)
                cancelAllWorkByTag(WidgetInitialLoadDataWorker.TAG)
            }
        }
    }

    override suspend fun doWork(): Result {
        val database = AppDatabase.with(applicationContext)
        val appWidgetIds = MealWidget.getAppWidgetIds(applicationContext)

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "doWork()")
        }

        if (appWidgetIds.isEmpty()) {
            disable(applicationContext)

            return Result.success()
        }

        val canteenIds = database.widgetConfiguration.getCanteenIdsByWidgetIds(appWidgetIds)

        val allOk = supervisorScope {
            val jobs = canteenIds.map { canteenId ->
                launch {
                    MealSyncing.syncCanteenSynchronousThrowEventually(
                        canteenId = canteenId,
                        force = false,
                        context = applicationContext
                    )
                }
            }

            jobs.map {
                try {
                    it.join()

                    false
                } catch (ex: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "task failed", ex)
                    }

                    true
                }
            }.none()
        }

        return if (allOk) Result.success()
        else Result.retry()
    }
}