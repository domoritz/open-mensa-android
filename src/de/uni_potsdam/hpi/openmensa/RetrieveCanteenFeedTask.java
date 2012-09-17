package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Canteens;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingCanteensListener;
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask;

/**
 * 
 * @author dominik
 * TODO: Parse the feed and write into the Hash instead of only saving the json
 */
public class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	private Canteens canteens;
	private OnFinishedFetchingCanteensListener fetchListener;
	protected String name = "Canteens";
	
	public RetrieveCanteenFeedTask(Context context, OnFinishedFetchingCanteensListener fetchListener) {
		super(context);
		this.canteens = new Canteens();
		this.fetchListener = fetchListener;
	}
	
	public Canteens getCanteens() {
		return canteens;
	}
	
	protected void parseFromJSON()  {
		Canteen[] canteens_arr = gson.fromJson(getFetchedJSON(), Canteen[].class);
		for(Canteen canteen : canteens_arr) {
			canteens.put(canteen.key, canteen);
		}
	}

	protected void onPostExecuteFinished() {	
		Log.d(TAG, String.format("Fetched %s canteen items", canteens.size()));
		
		// notify that we are done
		fetchListener.onCanteenFetchFinished(this);
	}
}