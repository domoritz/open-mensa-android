package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author dominik
 *
 */
class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	// wrap the canteen because the API v1 needs this
	private class WrappedCanteen {
		@SerializedName("cafeteria")
		public Canteen canteen;
	}
	
	private ArrayList<Canteen> canteenList;
	private OnFinishedFetchingCanteensListener fetchListener;
	
	public RetrieveCanteenFeedTask(Context context, OnFinishedFetchingCanteensListener fetchListener) {
		super(context);
		this.canteenList = new ArrayList<Canteen>();
		this.fetchListener = fetchListener;
	}
	
	public ArrayList<Canteen> getCanteenList() {
		return canteenList;
	}
	
	protected void parseFromJSON(String string)  {
		WrappedCanteen[] canteens = gson.fromJson(string, WrappedCanteen[].class);
		for(WrappedCanteen wrappedCanteen : canteens) {
			canteenList.add(wrappedCanteen.canteen);
		}
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("Fetched %s canteen items", canteenList.size()));
		
		// notify that we are done
		fetchListener.onCanteenFetchFinished(this);
	}
}