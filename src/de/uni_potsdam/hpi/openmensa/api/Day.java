package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dominik
 *
 */
public class Day {
	@SerializedName("date")
	public String date;
	
	@SerializedName("closed")
	public Boolean closed;
}
