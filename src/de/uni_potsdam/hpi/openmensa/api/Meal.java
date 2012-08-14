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
	
	@SerializedName("description")
	public String description;
	
	// TODO: maybe auto-parse into Date from ISO8601
	@SerializedName("date")
	public String date;
	
	public Meal(String name, String description, String date) {
		this.name = name;
		this.description = description;
		this.date = date;
	}
}
