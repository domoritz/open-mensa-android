package de.uni_potsdam.hpi.openmensa;

import org.json.JSONException;
import org.json.JSONObject;

public class Canteen {
	public String name = "Dummy";
	public String key = null;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}
	
	public Canteen(JSONObject json) throws JSONException {
		key = json.getString("id");
		name =json.getString("name");
	}
	
	public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", key );
        json.put("name", name );
        return json;
    }

	
	public Canteen() {}

	@Override
	public String toString() {
		return name;
	}
}
