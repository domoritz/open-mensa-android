package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dominik
 *
 */
public class Prices {
	@SerializedName("students")
	public float students;
	
	@SerializedName("employees")
	public float employees;
	
	@SerializedName("pupils")
	public float pupils;
	
	@SerializedName("others")
	public float others;
}
