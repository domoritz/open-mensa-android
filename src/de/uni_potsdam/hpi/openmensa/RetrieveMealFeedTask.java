package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Meal;
import de.uni_potsdam.hpi.openmensa.helpers.OnFinishedFetchingMealsListener;
import de.uni_potsdam.hpi.openmensa.helpers.RetrieveFeedTask;

/**
 * 
 * @author dominik
 */
public class RetrieveMealFeedTask extends RetrieveFeedTask {

	private ArrayList<Meal> mealList;
	private OnFinishedFetchingMealsListener fetchListener;
	protected String name = "Meals";
	protected Canteen canteen;
	private String date;

	public RetrieveMealFeedTask(Context context, Activity activity, OnFinishedFetchingMealsListener fetchListener, Canteen canteen, String date) {
		super(context, activity);
		this.mealList = new ArrayList<Meal>();
		this.canteen = canteen;
		this.date = date;
		this.fetchListener = fetchListener;
	}
	
	public ArrayList<Meal> getMealList() {
		return mealList;
	}
	
	Canteen getCanteen() {
		return canteen;
	}
	
	String getDate() {
		return date;
	}
	
	protected void parseFromJSON(String jsonString) {
		Meal[] meals = gson.fromJson(jsonString, Meal[].class);
		for(Meal meal : meals) {
			mealList.add(meal);
		}
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("Fetched %s meal items", mealList.size()));
		
		// notify that we are done
		fetchListener.onMealFetchFinished(this);
	}
}