package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Day;
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
	protected Canteen canteen;

	public RetrieveDaysFeedTask(Context context, OnFinishedFetchingDaysListener fetchListener, Canteen canteen) {
		super(context);
		this.canteen = canteen;
		this.fetchListener = fetchListener;
	}
	
	Days getDays() {
		return days;
	}
	
	Canteen getCanteen() {
		return canteen;
	}
	
	protected void parseFromJSON(String jsonString) {
		days = gson.fromJson(jsonString, Days.class);
		for (Day day : days) {
			if (day.meals == null || day.date == null) {
				Log.w(MainActivity.TAG, "Incomplete json response from server. Meals or date is null");
				this.exception = new Exception("Incomplete response from server.");
			}
		}
	}

	protected void onPostExecuteFinished() {
		if (days == null) {
			throw new IllegalStateException("Days cannot be null.");
		}
		Log.d(TAG, String.format("Fetched %s days", days.size()));
		
		// notify that we are done
		fetchListener.onDaysFetchFinished(this);
	}
}