package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import de.uni_potsdam.hpi.openmensa.api.Meal;
import de.uni_potsdam.hpi.openmensa.api.WrappedMeal;

/**
 * 
 * @author dominik
 */
class RetrieveMealFeedTask extends RetrieveFeedTask {

	private ArrayList<Meal> mealList;
	private OnFinishedFetchingMealsListener fetchListener;

	public RetrieveMealFeedTask(Context context, OnFinishedFetchingMealsListener fetchListener) {
		super(context);
		this.mealList = new ArrayList<Meal>();
		this.fetchListener = fetchListener;
	}
	
	public ArrayList<Meal> getMealList() {
		return mealList;
	}
	
	protected void parseFromJSON(String string) {
		WrappedMeal[] meals = gson.fromJson(string, WrappedMeal[].class);
		for(WrappedMeal wrappedMeal : meals) {
			mealList.add(wrappedMeal.meal);
		}
	}

	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("Fetched %s meal items", mealList.size()));
		
		// notify that we are done
		fetchListener.onMealFetchFinished(this);
	}
}