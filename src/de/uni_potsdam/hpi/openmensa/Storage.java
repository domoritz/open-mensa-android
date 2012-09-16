package de.uni_potsdam.hpi.openmensa;

import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import de.uni_potsdam.hpi.openmensa.api.Canteens;

/**
 * A wrapper around canteens that stores some metadata as well.
 *  
 * @author dominik
 *
 */

public class Storage {
	@SerializedName("canteens")
	Canteens canteens;
	
	@SerializedName("lastUpdate")
	public Calendar lastUpdate;
	
	public Boolean isOutOfDate() {
		if (lastUpdate == null) {
			Log.d(MainActivity.TAG, "Out of date because no last fetch date is set.");
			return true;
		}
		
		Calendar now = Calendar.getInstance();
		// 1 hour
		int maxDiff = 1000*60*60;
		if (now.getTimeInMillis() - lastUpdate.getTimeInMillis() > maxDiff) {
			return true;
		}
		return false;
	}
	
	public Canteens getCanteens(Context context) {
		if (canteens == null) {
			refreshStorage(context);
		}
		return canteens;
	}
	
	public Canteens getCanteens() {
		if (canteens == null) {
			return new Canteens();
		}
		return canteens;
	}
	
	public void setCanteens(Canteens canteens) {
		this.canteens = canteens;
	}

	public void saveCanteens(Context context, Canteens canteens) {
		setCanteens(canteens);
		lastUpdate = Calendar.getInstance();
		
		SettingsProvider.setStorage(context, this);
		SettingsProvider.refreshActiveCanteens(context);
	}

	/**
	 * Gets the canteens from the shared preferences without fetching
	 */
	public void refreshStorage(Context context) {
		Storage storage = SettingsProvider.getStorage(context);
		canteens = storage.getCanteens();
		lastUpdate = storage.lastUpdate;
	}
}
