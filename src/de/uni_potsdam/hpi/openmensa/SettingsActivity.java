package de.uni_potsdam.hpi.openmensa;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	public static final String KEY_SOURCE_URL = "pref_source_url";
	public static final String KEY_ACTIVE_CANTEENS = "pref_canteen";
	public static final String KEY_AVAILABLE_CANTEENS = "pref_available_canteens";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private static SharedPreferences getSharedPrefs(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static String getSourceUrl(Context context) {
    	String url = getSharedPrefs(context).getString(KEY_SOURCE_URL, context.getResources().getString(R.string.source_url_default));
    	return url;
    }
    
    public static Set<String> getActiveCanteens(Context context) {
    	Set<String> set = getSharedPrefs(context).getStringSet(KEY_ACTIVE_CANTEENS, new HashSet<String>());
    	return set;
    }

}