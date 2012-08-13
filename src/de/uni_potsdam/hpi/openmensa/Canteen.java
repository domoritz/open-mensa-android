package de.uni_potsdam.hpi.openmensa;

import com.google.gson.annotations.SerializedName;

public class Canteen {
	@SerializedName("name")
	public String name = "Dummy";
	
	@SerializedName("id")
	public String key = null;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}

	public Canteen() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return name;
	}
}
