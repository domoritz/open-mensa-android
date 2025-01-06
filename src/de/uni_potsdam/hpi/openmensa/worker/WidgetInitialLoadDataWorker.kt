package de.uni_potsdam.hpi.openmensa.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing

class WidgetInitialLoadDataWorker(context: Context, private val workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        private const val EXTRA_CANTEEN_ID = "canteenId"
        const val TAG = "WidgetInitialLoadDataWorker"
        private const val LOG_TAG = "WidgetInitialWorker"

        private fun workId(canteenId: Int) = "$TAG:$canteenId"

        fun enqueue(context: Context, canteenId: Int) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                workId(canteenId),
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<WidgetInitialLoadDataWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .addTag(TAG)
                    .setInputData(
                        Data.Builder()
                            .putInt(EXTRA_CANTEEN_ID, canteenId)
                            .build()
                    )
                    .build()
            )
        }
    }

    override suspend fun doWork(): Result {
        val canteenId = workerParameters.inputData.getInt(EXTRA_CANTEEN_ID, -1)

        try {
            MealSyncing.syncCanteenSynchronousThrowEventually(
                canteenId = canteenId,
                force = false,
                context = applicationContext
            )

            return Result.success()
        } catch (ex: Exception) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "task failed for $canteenId")
            }

            return Result.retry()
        }
    }
}