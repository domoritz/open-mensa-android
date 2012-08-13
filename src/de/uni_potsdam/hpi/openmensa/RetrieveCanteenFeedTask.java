package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	private ArrayList<Canteen> listItems;
	
	public RetrieveCanteenFeedTask(Context context) {
		super(context);
	}

	protected void parseFromJSON(String string) throws JSONException {
		JSONArray jsonArray = new JSONArray(string);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject root = jsonArray.getJSONObject(i);
			JSONObject meal = root.getJSONObject("meal");
			listItems.add(new Canteen(meal.getString("name"), meal
					.getString("description")));
		}
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("%s Items", listItems.size()));
	}
}