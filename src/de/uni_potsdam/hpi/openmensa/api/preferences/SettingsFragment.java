package de.uni_potsdam.hpi.openmensa.api.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;

import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.R;

import de.uni_potsdam.hpi.openmensa.api.Canteen;

/**
 * The fragment that displays the preferences.
 * 
 * @author dominik
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        EditTextPreference editTextPref = (EditTextPreference) findPreference(SettingsUtils.KEY_SOURCE_URL);
        editTextPref.setSummary(sp.getString(SettingsUtils.KEY_SOURCE_URL, editTextPref.getText()));

        ListPreference themePref = (ListPreference) findPreference(SettingsUtils.KEY_STYLE);
        themePref.setSummary(themePref.getEntry());

        updateFavouriteCanteensSummary();
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
        }

        if (key.equals(SettingsUtils.KEY_FAVOURITES)) {
            updateFavouriteCanteensSummary();
        }
    }

    private void updateFavouriteCanteensSummary() {
        SettingsUtils.updateFavouriteCanteensFromPreferences(MainActivity.getAppContext());
        ArrayList<Canteen> favouriteCanteens = SettingsUtils.getStorage(MainActivity.getAppContext()).getFavouriteCanteens();
        Preference pref = findPreference(SettingsUtils.KEY_FAVOURITES);
        int size = favouriteCanteens.size();
        if (size == 0) {
            pref.setSummary(getString(R.string.canteen_desc_empty));
        } else {
            pref.setSummary(String.format(getString(R.string.canteen_desc), size));
        }
    }
}