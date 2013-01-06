package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dominik
 *
 */
public class Day {
	@SerializedName("date")
	public String date;
	
	@SerializedName("closed")
	public Boolean closed;
	
	@SerializedName("meals")
	public Meals meals;
	
	@Override
	public String toString() {
		return date;
	}

	public Meals getMeals() {
		return meals;
	}
}
