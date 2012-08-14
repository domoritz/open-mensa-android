package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.WrappedCanteen;

/**
 * 
 * @author dominik
 *
 */
class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	private String fetchedJSON;
	private ArrayList<Canteen> canteenList;
	private OnFinishedFetchingCanteensListener fetchListener;
	
	public RetrieveCanteenFeedTask(Context context, OnFinishedFetchingCanteensListener fetchListener) {
		super(context);
		this.canteenList = new ArrayList<Canteen>();
		this.fetchListener = fetchListener;
	}
	
	public String getFetchedJSON() {
		return fetchedJSON;
	}
	
	public ArrayList<Canteen> getCanteenList() {
		return canteenList;
	}
	
	protected void parseFromJSON(String string)  {
		this.fetchedJSON = string;
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