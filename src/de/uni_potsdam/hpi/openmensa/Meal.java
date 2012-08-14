package de.uni_potsdam.hpi.openmensa;

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
	
	public Meal(String name, String description) {
		this.name = name;
		this.description = description;
	}
}
