package de.uni_potsdam.hpi.openmensa.api.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

import de.uni_potsdam.hpi.openmensa.R;

/**
 * 
 * @author dominik
 *
 */
public class SettingsActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefStyle = prefs.getString(SettingsUtils.KEY_STYLE, getString(R.string.pref_theme_default));
        setTheme(SettingsUtils.getThemeByString(prefStyle));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);

        // http://gmariotti.blogspot.de/2013/01/preferenceactivity-preferencefragment.html
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            getFragmentManager().beginTransaction()
                  .replace(android.R.id.content, new SettingsFragment())
                  .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.unregisterOnSharedPreferenceChangeListener(this);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(SettingsUtils.KEY_STYLE)) {
            recreate();
        }
    }
}