package de.uni_potsdam.hpi.openmensa.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class WidgetDataRefreshWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
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

    override fun doWork(): Result {
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

        val latch = CountDownLatch(canteenIds.size)
        val hadError = AtomicBoolean(false)

        canteenIds.forEach { canteenId ->
            MealSyncing.syncInBackground(
                canteenId = canteenId,
                force = false,
                context = applicationContext,
                onCompletion = {
                    if (it.isFailure) hadError.set(true)

                    latch.countDown()
                }
            )
        }

        latch.await()

        return if (hadError.get()) Result.retry()
        else Result.success()
    }
}