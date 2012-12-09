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
	
	// app only, not api
	@SerializedName("favourite")
	public Boolean favourite;
	
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
}
