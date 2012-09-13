package de.uni_potsdam.hpi.openmensa.api;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import de.uni_potsdam.hpi.openmensa.SettingsProvider;

@SuppressWarnings("serial")
public class Canteens extends HashMap<String, Canteen> {
	// app only, not api
	@SerializedName("lastUpdate")
	public Calendar lastUpdate;
	
	public Boolean isOutOfDate() {
		if (lastUpdate == null) {
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

	public void setData(Context context, Canteens canteens) {
		clear();
		putAll(canteens);
		lastUpdate = Calendar.getInstance();
		
		SettingsProvider.setCanteens(context, canteens);
		SettingsProvider.refreshActiveCanteens(context);
	}
}