package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dominik
 *
 */
public class Prices {
	@SerializedName("students")
	public int students;
	
	@SerializedName("employees")
	public int employees;
	
	@SerializedName("pupils")
	public int pupils;
	
	@SerializedName("others")
	public int others;
}
