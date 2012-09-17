package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
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

	public RetrieveMealFeedTask(Context context, OnFinishedFetchingMealsListener fetchListener) {
		super(context);
		this.mealList = new ArrayList<Meal>();
		this.fetchListener = fetchListener;
	}
	
	public ArrayList<Meal> getMealList() {
		return mealList;
	}
	
	protected void parseFromJSON() {
		Meal[] meals = gson.fromJson(getFetchedJSON(), Meal[].class);
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