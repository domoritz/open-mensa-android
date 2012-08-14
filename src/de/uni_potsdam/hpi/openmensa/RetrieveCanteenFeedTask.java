package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;

class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	private ArrayList<Canteen> listItems;
	
	public RetrieveCanteenFeedTask(Context context) {
		super(context);
	}

	protected void parseFromJSON(String string)  {
		Canteen[] canteens = gson.fromJson(string, Canteen[].class);
		listItems.addAll(new ArrayList<Canteen>(Arrays.asList(canteens)));
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("%s Items", listItems.size()));
	}
}