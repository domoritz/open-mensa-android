package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Days;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingDaysListener;
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask;

/**
 * 
 * @author dominik
 */
public class RetrieveDaysFeedTask extends RetrieveFeedTask {

	private Days days;
	private OnFinishedFetchingDaysListener fetchListener;
	protected String name = "Days";

	public RetrieveDaysFeedTask(Context context, OnFinishedFetchingDaysListener fetchListener) {
		super(context);
		this.fetchListener = fetchListener;
		this.visible = true;
	}
	
	Days getDays() {
		return days;
	}
	
	protected void parseFromJSON(String jsonString) {
		days = gson.fromJson(jsonString, Days.class);
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("Fetched days"));
		
		// notify that we are done
		fetchListener.onDaysFetchFinished(this);
	}
}