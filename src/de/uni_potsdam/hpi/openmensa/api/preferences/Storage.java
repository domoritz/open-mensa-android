package de.uni_potsdam.hpi.openmensa.api.preferences;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.api.Canteen;
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
	
	@SerializedName("currentCanteen")
	public String currentCanteen;
	
	@SerializedName("lastUpdate")
	public Calendar lastUpdate;
	
	public Boolean isOutOfDate() {
		if (lastUpdate == null) {
			Log.d(MainActivity.TAG, "Out of date because no last fetch date is set.");
			return true;
		}
		
		Calendar now = Calendar.getInstance();
		// 48 hours
		int maxDiff = 1000*60*60*48;
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
		if (canteens == null)
			canteens = new Canteens();

		return canteens;
	}
	
	public void setCanteens(Canteens newCanteens) {
		getCanteens().update(newCanteens);
	}

	public void saveCanteens(Context context, Canteens canteens) {
		setCanteens(canteens);
		lastUpdate = Calendar.getInstance();
		
		flush(context);
		SettingsProvider.refreshActiveCanteens(context);
	}
	
	public ArrayList<Canteen> getFavouriteCanteens() {
		ArrayList<Canteen> favouriteCanteens = new ArrayList<Canteen>();
		for (Canteen canteen : getCanteens().values()) {
			if (canteen.isFavourite())
				favouriteCanteens.add(canteen);
		}
		return favouriteCanteens;
	}

	/**
	 * Gets the canteens from the shared preferences without fetching
	 * opposite: flush
	 */
	public void refreshStorage(Context context) {
		Storage storage = SettingsProvider.getStorage(context);
		canteens = storage.canteens;
		lastUpdate = storage.lastUpdate;
		currentCanteen = storage.currentCanteen;
	}
	
	/**
	 * saves the storage object in the shared preferences
	 * opposite: refreshStorage
	 * 
	 * @param context
	 */
	public void flush(Context context) {
		SettingsProvider.setStorage(context, this);
	}

	public void setCurrentCanteen(Canteen canteen) {
		currentCanteen = canteen.key;
	}

	public Canteen getCurrentCanteen() {
		if (currentCanteen == null || currentCanteen.isEmpty()) {
			if (getFavouriteCanteens().size() > 0) {
				currentCanteen = getFavouriteCanteens().get(0).key;
			} else {
				return null;
			}	
		}
		return getCanteens().get(currentCanteen);
	}

	/**
	 * Return true if we don't have any canteens in the storage
	 */
	public boolean isEmpty() {
		return getCanteens().size() == 0;
	}
}
