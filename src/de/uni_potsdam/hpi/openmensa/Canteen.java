package de.uni_potsdam.hpi.openmensa;

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
	
	//@SerializedName("address")
	//public String address;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}

	@Override
	public String toString() {
		return name;
	}
}
