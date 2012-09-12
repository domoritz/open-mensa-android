package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dominik
 *
 */
public class Meal {
	@SerializedName("name")
	public String name;
	
	@SerializedName("notes")
	public String[] notes;
	
	@SerializedName("category")
	public String category;
	
	// TODO: maybe auto-parse into Date from ISO8601
	@SerializedName("date")
	public String date;
	
	public Meal(String name, String date) {
		this.name = name;
		this.date = date;
	}
}
