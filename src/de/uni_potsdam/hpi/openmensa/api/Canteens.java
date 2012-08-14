package de.uni_potsdam.hpi.openmensa.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class Canteens extends HashMap<String, Canteen> {
	
	// TODO make this sorted when we save this as json and got rid of the getStringSet
	Set<String> activeCanteens = new HashSet<String>();
	
	String displayedCanteen = "0";
	
	// TODO: calculate this from displayedCanteen and activeCanteens
	int displayedCanteenPosition = 0;
}
