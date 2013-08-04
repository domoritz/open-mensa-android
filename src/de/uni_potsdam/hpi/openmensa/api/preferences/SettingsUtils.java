package de.uni_potsdam.hpi.openmensa.api.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

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
    public static int getThemeByString (String theme) {
        if (theme.equalsIgnoreCase("dark")) {
            return R.style.AppTheme;
        } else if (theme.equalsIgnoreCase("light")) {
            return R.style.AppThemeLight;
        } else {
            Log.e(MainActivity.TAG, "Theme not found");
            return R.style.AppTheme;
        }
    }

    /**
     * saves the active canteens from the preferences in the storage object
     * @param context
     */
	public static void updateFavouriteCanteensFromPreferences(Context context) {
		// TODO: getStringSet requires API Level 11
		Set<String> favouriteCanteenKeys = getSharedPrefs(context).getStringSet(KEY_FAVOURITES, new HashSet<String>());
		Log.d(MainActivity.TAG, String.format("Got favourites %s", favouriteCanteenKeys));
		Storage storage = getStorage(context);
		storage.setFavouriteCanteens(favouriteCanteenKeys);
		storage.saveToPreferences(context);
	}
}
