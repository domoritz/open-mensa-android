package de.uni_potsdam.hpi.openmensa.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import de.uni_potsdam.hpi.openmensa.BuildConfig

class VersionPreference(context: Context, attributeSet: AttributeSet): Preference(context, attributeSet) {
    init {
        summary = BuildConfig.VERSION_NAME
    }
}