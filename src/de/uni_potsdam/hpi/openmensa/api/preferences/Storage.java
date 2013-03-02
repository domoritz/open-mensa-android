package de.uni_potsdam.hpi.openmensa.api.preferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
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
	/*========
	 * Don't forget to add the members to loadFromPreferences() 
	 */
	
	@SerializedName("canteens")
	Canteens canteens;
	
	/**
	 * ID of the canteen that is shown in the app
	 */
	@SerializedName("currentCanteen")
	public String currentCanteen;
	
	@SerializedName("lastUpdate")
	public Calendar lastCanteensUpdate;
	
	/**
	 * IDs of favourite canteens
	 */
	@SerializedName("favouriteCanteensKeys")
	public Set<String> favouriteCanteensKeys = new HashSet<String>();
	

	public Boolean areCanteensOutOfDate() {
		if (lastCanteensUpdate == null) {
			Log.d(MainActivity.TAG, "Out of date because no last fetch date is set.");
			return true;
		}
		
		Calendar now = Calendar.getInstance();
		// 14 days
		int maxDiff = 1000*60*60*24*14;
		if (now.getTimeInMillis() - lastCanteensUpdate.getTimeInMillis() > maxDiff) {
			return true;
		}
		return false;
	}
	
	public Canteens getCanteens(Context context) {
		if (canteens == null) {
			loadFromPreferences(context);
		}
		return getCanteens();
	}
	
	public Canteens getCanteens() {
		if (canteens == null)
			canteens = new Canteens();

		return canteens;
	}
	
	public void setCanteens(Canteens newCanteens) {
		getCanteens().clear();
		canteens.putAll(newCanteens);
	}

	public void saveCanteens(Context context, Canteens canteens) {
		setCanteens(canteens);
		lastCanteensUpdate = Calendar.getInstance();
		
		saveToPreferences(context);
		SettingsProvider.updateFavouriteCanteensFromPreferences(context);
	}
	
	public ArrayList<Canteen> getFavouriteCanteens() {
		ArrayList<Canteen> favouriteCanteens = new ArrayList<Canteen>();
		Canteens canteens = getCanteens();
		for (String key : favouriteCanteensKeys) {
			Canteen canteen = canteens.get(key);
			if (canteen != null)
				favouriteCanteens.add(canteen);
			else
				Log.w(MainActivity.TAG, String.format("A favourite canteen was requested that is not in the storage. Key: %s", key));
		}
		return favouriteCanteens;
	}

	/**
	 * Gets the canteens from the shared preferences without fetching
	 * opposite: saveToPreferences
	 * 
	 * @param context
	 */
	public void loadFromPreferences(Context context) {
		Storage storage = SettingsProvider.getStorage(context);
		canteens = storage.canteens;
		lastCanteensUpdate = storage.lastCanteensUpdate;
		currentCanteen = storage.currentCanteen;
		favouriteCanteensKeys = storage.favouriteCanteensKeys;

		SettingsProvider.updateFavouriteCanteensFromPreferences(context);
	}
	
	/**
	 * saves the storage object in the shared preferences
	 * opposite: loadFromPreferences
	 * 
	 * @param context
	 */
	public void saveToPreferences(Context context) {
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

	public void setFavouriteCanteens(Set<String> favourites) {
		Log.d(MainActivity.TAG, String.format("Update favourites: %s", favourites));
		favouriteCanteensKeys = favourites;
	}
}
