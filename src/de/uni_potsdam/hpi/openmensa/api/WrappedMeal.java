package de.uni_potsdam.hpi.openmensa.api;

import com.google.gson.annotations.SerializedName;

/**
 * Wrap the Meal because the API v1 needs this
 * 
 * @author Christian
 */
public class WrappedMeal {
	@SerializedName("meal")
	public Meal meal;
}
