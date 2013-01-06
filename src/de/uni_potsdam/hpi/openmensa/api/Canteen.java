package de.uni_potsdam.hpi.openmensa.api;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dominik
 *
 */
public class Canteen {	
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
	@SerializedName("_favourite")
	public Boolean favourite;

	// date -> meals
	@SerializedName("_days")
	public HashMap<String, Day> days;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}
	
	public Boolean isFavourite() {
		if (favourite != null) {
			return favourite;
		}
		return false;
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
		}
	}
	
	public Day getDay(String date) {
		if (days == null) {
			return null;
		}
		return days.get(date);
	}
}
