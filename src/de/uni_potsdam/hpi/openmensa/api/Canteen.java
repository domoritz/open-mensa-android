package de.uni_potsdam.hpi.openmensa.api;

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
	
	@SerializedName("longitude")
	public Float longitude;
	
	@SerializedName("latitude")
	public Float latitude;
	
	// app only, not api
	@SerializedName("active")
	public Boolean active;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}
	
	public Boolean isActive() {
		if (active != null) {
			return active;
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
}
