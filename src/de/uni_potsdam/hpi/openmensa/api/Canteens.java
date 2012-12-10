package de.uni_potsdam.hpi.openmensa.api;

import java.util.HashMap;

/**
 * 
 * @author dominik
 *
 */
@SuppressWarnings("serial")
public class Canteens extends HashMap<String, Canteen> {

	public void update(Canteens canteens) {
		Canteens result = new Canteens();
		for (String key: canteens.keySet()) {
			Canteen newCanteen = canteens.get(key);
			Canteen canteen = this.get(key);
			if (canteen != null) {
				newCanteen.favourite = canteen.favourite;
			}
			result.put(key, newCanteen);
		}
		
		this.clear();
		this.putAll(result);
	}
}