package de.uni_potsdam.hpi.openmensa

import android.app.Activity
import android.content.Context
import android.util.Log
import de.uni_potsdam.hpi.openmensa.api.client.HttpApiClient
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingCanteensListener
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveAsyncTask
import de.uni_potsdam.hpi.openmensa.sync.SyncUtil

/**
 *
 * @author dominik
 * TODO: Parse the feed and write into the Hash instead of only saving the json
 */
class RetrieveCanteenFeedTask(context: Context, activity: Activity, private val fetchListener: OnFinishedFetchingCanteensListener) : RetrieveAsyncTask(context, activity) {
    init {
        this.visible = true
    }

    override fun doInBackground(vararg strings: String): Void? {
        SyncUtil.syncCanteenList(
                api = HttpApiClient.getInstance(context),
                database = AppDatabase.with(context)
        )

        return null
    }

    override fun onPostExecuteFinished() {
        // notify that we are done
        fetchListener.onCanteenFetchFinished(this)
    }
}