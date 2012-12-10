package de.uni_potsdam.hpi.openmensa.api;

import java.util.ArrayList;
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
	@SerializedName("favourite")
	public Boolean favourite;
	
	@SerializedName("days")
	public Days days;

	// date -> meals
	@SerializedName("meals")
	public HashMap<String, ArrayList<Meal>> meals;
	
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
	
	public ArrayList<Meal> getMealList(String date) {
		if (meals == null)
			meals = new HashMap<String, ArrayList<Meal>>();
		return meals.get(date);
	}
	
	public void setMealList(String date, ArrayList<Meal> mealList) {
		if (meals == null)
			meals = new HashMap<String, ArrayList<Meal>>();
		meals.put(date, mealList);
	}
	
	public Days getDays() {
		return days;
	}

	public void setDays(Days days) {
		this.days = days;
	}

	@Override
	public String toString() {
		return name;
	}
}
