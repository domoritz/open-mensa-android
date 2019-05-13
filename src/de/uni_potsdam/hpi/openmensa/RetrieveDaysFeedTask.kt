package de.uni_potsdam.hpi.openmensa

import android.app.Activity
import android.content.Context
import android.util.Log
import de.uni_potsdam.hpi.openmensa.api.Canteen
import de.uni_potsdam.hpi.openmensa.api.Day
import de.uni_potsdam.hpi.openmensa.api.Days
import de.uni_potsdam.hpi.openmensa.api.client.HttpApiClient
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingDaysListener
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveAsyncTask
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask
import de.uni_potsdam.hpi.openmensa.sync.SyncUtil

/**
 *
 * @author dominik
 */
class RetrieveDaysFeedTask(context: Context, activity: Activity, private val fetchListener: OnFinishedFetchingDaysListener, private val canteenId: Int) : RetrieveAsyncTask(context, activity) {
    override fun doInBackground(vararg params: String?): Void? {
        SyncUtil.syncDaysAndMeals(
                api = HttpApiClient.getInstance(context),
                database = AppDatabase.with(context),
                canteenId = canteenId
        )

        return null
    }

    override fun onPostExecuteFinished() {
        // notify that we are done
        fetchListener.onDaysFetchFinished(this)
    }
}