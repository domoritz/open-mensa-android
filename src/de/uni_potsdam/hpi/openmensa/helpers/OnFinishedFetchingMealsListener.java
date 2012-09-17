package de.uni_potsdam.hpi.openmensa.helpers;

import de.uni_potsdam.hpi.openmensa.RetrieveMealFeedTask;

public interface OnFinishedFetchingMealsListener {
	
	void onMealFetchFinished(RetrieveMealFeedTask task);
}
