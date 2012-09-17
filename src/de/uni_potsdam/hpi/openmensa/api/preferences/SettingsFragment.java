package de.uni_potsdam.hpi.openmensa.api.preferences;

import de.uni_potsdam.hpi.openmensa.R;
import de.uni_potsdam.hpi.openmensa.R.xml;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * The fragment that displays the preferences.
 * 
 * @author dominik
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}