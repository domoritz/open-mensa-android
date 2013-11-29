package de.uni_potsdam.hpi.openmensa.api.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.R;

/**
 * Provides simple methods to access shared settings.
 * 
 * @author dominik
 *
 */
public class SettingsUtils {

	public static final String KEY_SOURCE_URL = "pref_source_url";
	public static final String KEY_STORAGE = "om_storage";
	
	// Make sure to update xml/preferences.xml as well
	public static final String KEY_FAVOURITES = "pref_favourites";

    public static final String KEY_STYLE = "pref_style";

	private static Gson gson = new Gson();
	
    
    private static SharedPreferences getSharedPrefs(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static String getSourceUrl(Context context) {
    	return getSharedPrefs(context).getString(KEY_SOURCE_URL, context.getResources().getString(R.string.source_url_default));

    }
    
    public static Storage getStorage(Context context) {
    	// Throws ClassCastException if there is a preference with this name that is not a Set.
    	String json = getSharedPrefs(context).getString(KEY_STORAGE, "{}");
    	return gson.fromJson(json, Storage.class);
    }
    
    public static void setStorage(Context context, Storage storage) {
    	String json = gson.toJson(storage);
    	//Log.d("json", json);
    	SharedPreferences.Editor editor = getSharedPrefs(context).edit();
    	editor.putString(SettingsUtils.KEY_STORAGE, json);
    	editor.commit();
    }
    public static int getThemeByString(String theme) {
        if (theme.equalsIgnoreCase("dark")) {
            return com.actionbarsherlock.R.style.Theme_Sherlock;
        } else if (theme.equalsIgnoreCase("light")) {
            return com.actionbarsherlock.R.style.Theme_Sherlock_Light;
        } else {
            Log.w(MainActivity.TAG, "Theme not found");
            return com.actionbarsherlock.R.style.Theme_Sherlock;
        }
    }

    /**
     * saves the active canteens from the preferences in the storage object
     * @param context Application Context
     */
	public static void updateFavouriteCanteensFromPreferences(Context context) {
        Set<String> favouriteCanteenKeys;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            favouriteCanteenKeys = getSharedPrefs(context).getStringSet(KEY_FAVOURITES, new HashSet<String>());
        } else {
            favouriteCanteenKeys = new HashSet(getStringArrayPref(context, KEY_FAVOURITES));
        }
		Log.d(MainActivity.TAG, String.format("Got favourites %s", favouriteCanteenKeys));
		Storage storage = getStorage(context);
		storage.setFavouriteCanteens(favouriteCanteenKeys);
		storage.saveToPreferences(context);
	}

    // http://stackoverflow.com/a/7361989/214950
    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}
