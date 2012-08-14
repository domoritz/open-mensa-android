package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author dominik
 *
 */
class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	private ArrayList<Canteen> canteenList;
	private OnFinishedFetchingCanteensListener fetchListener;
	
	public RetrieveCanteenFeedTask(Context context, OnFinishedFetchingCanteensListener fetchListener) {
		super(context);
		this.canteenList = new ArrayList<Canteen>();
		this.fetchListener = fetchListener;
	}
	
	protected void parseFromJSON(String string)  {
		Canteen[] canteens = gson.fromJson(string, Canteen[].class);
		canteenList.addAll(new ArrayList<Canteen>(Arrays.asList(canteens)));
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("Fetched %s canteen items", canteenList.size()));
		
		// notify that we are done
		fetchListener.onCanteenFetchFinished(canteenList);
	}
}