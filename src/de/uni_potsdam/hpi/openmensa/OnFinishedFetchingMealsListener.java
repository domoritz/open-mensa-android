package de.uni_potsdam.hpi.openmensa;

import java.util.List;

public interface OnFinishedFetchingMealsListener {
	
	void onMealFetchFinished(List<Meal> meals);
}
