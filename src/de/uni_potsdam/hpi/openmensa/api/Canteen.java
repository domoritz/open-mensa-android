package de.uni_potsdam.hpi.openmensa.api;

import java.util.Calendar;
import java.util.HashMap;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsProvider;
import de.uni_potsdam.hpi.openmensa.helpers.SpinnerItem;

/**
 * 
 * @author dominik
 *
 */
public class Canteen implements SpinnerItem {
	
	// 1 hour
	private static final int DAY_OUTDATED = 1000*60*60*1;
	
	@SerializedName("id")
	public String key = null;
	
	@SerializedName("name")
	public String name = "Dummy";
	
	@SerializedName("address")
	public String address;
	
	@SerializedName("coordinates")
	public Float[] coordinates;
	
	//==================
	// app only, not api

	/**
	 * date -> meals
	 */
	@SerializedName("_days")
	public HashMap<String, Day> days;
	
	/**
	 * Save when we last fetched for each day
	 */
	@SerializedName("_updates")
	public HashMap<String, Long> updates;

	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}

	@Override
	public String toString() {
		return name;
	}

	public void updateDays(Days newDays) {
		if (days == null)
			days = new HashMap<String, Day>();
		for (Day day : newDays) {
			days.put(day.date, day);
			justUpdated(day.date);
		}
	}
	
	public Day getDay(String date) {
		if (days == null) {
			return null;
		}
		return days.get(date);
	}
	
	public void justUpdated(String date) {
		if (updates == null)
			updates = new HashMap<String, Long>();
		
		Calendar now = Calendar.getInstance();
		updates.put(date, now.getTimeInMillis());
	}

	/**
	 * Normally, you should fetch whenever no day is present,
	 * but some open mensa feeds are not complete. So let's see whether
	 * we have already fetched the information for a certain day lately.
	 * 
	 * @param string the date as string
	 * @return
	 */
	public boolean isOutOfDate(String date) {
		if (updates == null)
			return true;
		
		Calendar now = Calendar.getInstance();
		Long lastUpdate = updates.get(date);
		
		if (lastUpdate == null)
			return true;
		
		if (now.getTimeInMillis() - lastUpdate > DAY_OUTDATED) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean execute(MainActivity mainActivity, int itemPosition) {
		Canteen c = SettingsProvider.getStorage(mainActivity).getFavouriteCanteens().get(itemPosition);
		Log.d(MainActivity.TAG, String.format("Chose canteen %s", c.key));
		mainActivity.changeCanteenTo(c);
		return true;
	}
}
