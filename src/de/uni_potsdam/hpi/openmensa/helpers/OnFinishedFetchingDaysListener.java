package de.uni_potsdam.hpi.openmensa.helpers;

import de.uni_potsdam.hpi.openmensa.RetrieveDaysFeedTask;

public interface OnFinishedFetchingDaysListener {

	void onDaysFetchFinished(RetrieveDaysFeedTask task);
}
