package de.uni_potsdam.hpi.openmensa;

import java.util.List;

public interface OnFinishedFetchingCanteensListener {
	
	void onCanteenFetchFinished(List<Canteen> canteens);
}
