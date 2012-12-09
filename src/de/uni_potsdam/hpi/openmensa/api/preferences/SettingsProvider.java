package de.uni_potsdam.hpi.openmensa.api.preferences;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.R;
import de.uni_potsdam.hpi.openmensa.api.Canteen;

/**
 * Provides simple methods to access shared settings.
 * 
 * @author dominik
 *
 */
public class SettingsProvider {

	public static final String KEY_SOURCE_URL = "pref_source_url";
	public static final String KEY_CANTEENS = "pref_canteens";
	public static final String KEY_ACTIVE_CANTEENS = "pref_active_canteens";
	
	private static Gson gson = new Gson();
	
    
    private static SharedPreferences getSharedPrefs(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static String getSourceUrl(Context context) {
    	String url = getSharedPrefs(context).getString(KEY_SOURCE_URL, context.getResources().getString(R.string.source_url_default));
    	return url;
    }
    
    public static Storage getStorage(Context context) {
    	// Throws ClassCastException if there is a preference with this name that is not a Set.
    	String json = getSharedPrefs(context).getString(KEY_CANTEENS, "{}");
    	Storage storage = gson.fromJson(json, Storage.class);
		return storage;
    }
    
    public static void setStorage(Context context, Storage storage) {
    	String json = gson.toJson(storage);
    	//Log.d("json", json);
    	SharedPreferences.Editor editor = getSharedPrefs(context).edit();
    	editor.putString(SettingsProvider.KEY_CANTEENS, json);
    	editor.commit();
    }

    /**
     * sets the active canteens in the canteens object
     * @param context
     */
	public static void refreshActiveCanteens(Context context) {
		Set<String> activeCanteensKeys = getSharedPrefs(context).getStringSet(KEY_ACTIVE_CANTEENS, new HashSet<String>());
		Storage storage = getStorage(context);
		for (Canteen canteen : storage.canteens.values()) {
			canteen.favourite = activeCanteensKeys.contains(canteen.key);
		}
		setStorage(context, storage);
	}
}
