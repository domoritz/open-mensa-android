package de.uni_potsdam.hpi.openmensa.helpers;

import de.uni_potsdam.hpi.openmensa.RetrieveCanteenFeedTask;

public interface OnFinishedFetchingCanteensListener {
	
	void onCanteenFetchFinished(RetrieveCanteenFeedTask task);
}
