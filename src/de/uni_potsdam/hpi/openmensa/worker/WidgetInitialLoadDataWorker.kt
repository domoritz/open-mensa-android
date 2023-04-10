package de.uni_potsdam.hpi.openmensa.worker

import android.content.Context
import androidx.work.*
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class WidgetInitialLoadDataWorker(context: Context, private val workerParameters: WorkerParameters): Worker(context, workerParameters) {
    companion object {
        private const val EXTRA_CANTEEN_ID = "canteenId"
        const val TAG = "WidgetInitialLoadDataWorker"

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

    override fun doWork(): Result {
        val canteenId = workerParameters.inputData.getInt(EXTRA_CANTEEN_ID, -1)

        val latch = CountDownLatch(1)
        val hadError = AtomicBoolean(false)

        MealSyncing.syncInBackground(
            canteenId = canteenId,
            force = false,
            context = applicationContext,
            onCompletion = {
                if (it.isFailure) hadError.set(true)

                latch.countDown()
            }
        )

        latch.await()

        return if (hadError.get()) Result.retry()
        else Result.success()
    }
}