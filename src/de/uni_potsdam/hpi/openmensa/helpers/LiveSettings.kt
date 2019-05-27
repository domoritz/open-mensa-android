package de.uni_potsdam.hpi.openmensa.helpers

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

object LiveSettings {
    fun <T> createObservablePreference(prefs: SharedPreferences, keyToWatch: String, readValue: () -> T) = object: LiveData<T>() {
        val listener = object: SharedPreferences.OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
                if (key == keyToWatch) {
                    refresh()
                }
            }
        }

        var hadValue = false

        fun refresh() {
            val newValue = readValue()

            if (newValue != value || !hadValue) {
                postValue(newValue)
                hadValue = true
            }
        }

        override fun onActive() {
            super.onActive()

            prefs.registerOnSharedPreferenceChangeListener(listener)
            refresh()
        }

        override fun onInactive() {
            super.onInactive()

            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}