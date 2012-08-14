package de.uni_potsdam.hpi.openmensa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.api.Canteen;

/**
 * 
 * @author dominik
 *
 */
public class SettingsProvider {

	public static final String KEY_SOURCE_URL = "pref_source_url";
	public static final String KEY_ACTIVE_CANTEENS = "pref_canteen";
	public static final String KEY_AVAILABLE_CANTEENS = "pref_available_canteens";
	
	private static Gson gson = new Gson();
	
    
    private static SharedPreferences getSharedPrefs(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static String getSourceUrl(Context context) {
    	String url = getSharedPrefs(context).getString(KEY_SOURCE_URL, context.getResources().getString(R.string.source_url_default));
    	return url;
    }
    
    public static Set<String> getActiveCanteens(Context context) {
    	// Throws ClassCastException if there is a preference with this name that is not a Set.
    	Set<String> set = getSharedPrefs(context).getStringSet(KEY_ACTIVE_CANTEENS, new HashSet<String>());
    	return set;
    }
    
    public static HashMap<String, Canteen> getAvailableCanteens(Context context) {
    	HashMap<String, Canteen> availableCanteens = new HashMap<String, Canteen>();
    	String json = getSharedPrefs(context).getString(KEY_AVAILABLE_CANTEENS, "[]");
		Canteen[] canteens = gson.fromJson(json, Canteen[].class);
		for (Canteen canteen : canteens) {
			availableCanteens.put(canteen.key, canteen);
		}
		return availableCanteens;
    }
}
