package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * Wrap the Canteen because the API v1 needs this
 * 
 * @author Christian
 */
public class WrappedCanteen {
	@SerializedName("cafeteria")
	public Canteen canteen;
}
