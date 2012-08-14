package de.uni_potsdam.hpi.openmensa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.WrappedCanteen;

/**
 * Provides simple methods to access shared settings.
 * TODO: Save Canteens object. This will improve the overall design and refactor the code. 
 * TODO: Use the canteens object to save the active canteens and the currently displayed canteen
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
    
    // TODO: see above for refactoring
    public static Set<String> getActiveCanteens(Context context) {
    	// Throws ClassCastException if there is a preference with this name that is not a Set.
    	Set<String> set = getSharedPrefs(context).getStringSet(KEY_ACTIVE_CANTEENS, new HashSet<String>());
    	return set;
    }
    
 // TODO: see above for refactoring
    public static HashMap<String, Canteen> getAvailableCanteens(Context context) {
    	HashMap<String, Canteen> availableCanteens = new HashMap<String, Canteen>();
    	String json = getSharedPrefs(context).getString(KEY_AVAILABLE_CANTEENS, "[]");
    	WrappedCanteen[] canteens = gson.fromJson(json, WrappedCanteen[].class);
		for(WrappedCanteen wrappedCanteen : canteens) {
			availableCanteens.put(wrappedCanteen.canteen.key, wrappedCanteen.canteen);
		}
		return availableCanteens;
    }
    
    // TODO: 
    // TODO: see above for refactoring
    public static void setAvailableCanteens(Context context, String json) {
    	SharedPreferences.Editor editor = getSharedPrefs(context).edit();
    	editor.putString(SettingsProvider.KEY_AVAILABLE_CANTEENS, json);
    	editor.commit();
    }
}
