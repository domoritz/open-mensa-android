package de.uni_potsdam.hpi.openmensa.api.preferences

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

import de.uni_potsdam.hpi.openmensa.R

import de.uni_potsdam.hpi.openmensa.ui.canteenlist.SelectCanteenDialogFragment

/**
 * The fragment that displays the preferences.
 *
 * @author dominik
 */
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        val sp = preferenceScreen.sharedPreferences
        val editTextPref = findPreference(SettingsUtils.KEY_SOURCE_URL) as EditTextPreference
        editTextPref.summary = SettingsUtils.getSourceUrl(context!!)

        val themePref = findPreference(SettingsUtils.KEY_STYLE) as ListPreference
        themePref.summary = themePref.entry

        val favouritePreference = findPreference(SettingsUtils.KEY_FAVOURITES)
        favouritePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            SelectCanteenDialogFragment().show(fragmentManager!!)

            false
        }

        val model = ViewModelProviders.of(this).get(SettingsModel::class.java)

        model.favoriteCanteensCounter.observe(this, Observer { size ->
            if (size == 0) {
                favouritePreference.summary = getString(R.string.canteen_desc_empty)
            } else {
                favouritePreference.summary = String.format(getString(R.string.canteen_desc), size)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        val pref = findPreference(key)
        if (pref is EditTextPreference) {
            pref.setSummary(pref.text)
        }
    }
}