package de.uni_potsdam.hpi.openmensa.api.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.R;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.SelectCanteenDialogFragment;

/**
 * The fragment that displays the preferences.
 * 
 * @author dominik
 */
public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        EditTextPreference editTextPref = (EditTextPreference) findPreference(SettingsUtils.KEY_SOURCE_URL);
        editTextPref.setSummary(SettingsUtils.INSTANCE.getSourceUrl(getContext()));

        ListPreference themePref = (ListPreference) findPreference(SettingsUtils.KEY_STYLE);
        themePref.setSummary(themePref.getEntry());

        Preference favouritePreference = findPreference(SettingsUtils.KEY_FAVOURITES);
        favouritePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new SelectCanteenDialogFragment().show(getFragmentManager());

                return false;
            }
        });

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
        SettingsUtils.INSTANCE.updateFavouriteCanteensFromPreferences(MainActivity.getAppContext());
        ArrayList<Canteen> favouriteCanteens = SettingsUtils.INSTANCE.getStorage(MainActivity.getAppContext()).getFavouriteCanteens();
        Preference pref = findPreference(SettingsUtils.KEY_FAVOURITES);
        int size = favouriteCanteens.size();
        if (size == 0) {
            pref.setSummary(getString(R.string.canteen_desc_empty));
        } else {
            pref.setSummary(String.format(getString(R.string.canteen_desc), size));
        }
    }
}