package de.uni_potsdam.hpi.openmensa.helpers

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import de.uni_potsdam.hpi.openmensa.BuildConfig

import de.uni_potsdam.hpi.openmensa.api.DefaultApiUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Provides simple methods to access shared settings.
 *
 * @author dominik
 */
class SettingsUtils(context: Application) {
    companion object {
        private const val LOG_TAG = "SettingsUtils"

        const val KEY_SOURCE_URL = "pref_source_url"

        // Make sure to update xml/preferences.xml as well
        const val KEY_FAVOURITES = "pref_favourites"

        const val KEY_LAST_SELECTED_CANTEEN_ID = "last_canteen_id"
        const val KEY_LAST_CANTEEN_LIST_UPDATE = "last_canteen_list_update"
        const val DID_MIGRATE_TO_SAFE_URL = "did_migrate_to_safe_url"
        // note: the current selection is the first selection
        // the last recently used items should be at the start of the list
        const val SELECTED_CITIES = "selected_cities"
        const val KEY_LAST_APP_VERSION = "last_app_version"

        private val lock = Object()
        private var instance: SettingsUtils? = null

        fun with(context: Context): SettingsUtils {
            if (instance == null) {
                synchronized(lock) {
                    if (instance == null) {
                        instance = SettingsUtils(context.applicationContext as Application)
                    }
                }
            }

            return instance!!
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    data class Settings(
        val sourceUrl: String?,
        val lastSelectedCanteenId: Int?,
        val favoriteCanteenIds: Set<Int>,
        val cityHistory: List<String>
    )

    private val settingsFlowInternal = MutableStateFlow(Settings(
        sourceUrl = sourceUrl.let { if (it.isBlank()) null else it },
        lastSelectedCanteenId = lastSelectedCanteenId,
        favoriteCanteenIds = favoriteCanteens,
        cityHistory = selectedCities
    ))

    val settingsFlow: StateFlow<Settings> = settingsFlowInternal

    var sourceUrl: String
        get() = prefs.getString(KEY_SOURCE_URL, "")!!
        set(value) {
            prefs.edit()
                    .putString(KEY_SOURCE_URL, value)
                    .apply()

            settingsFlowInternal.update {
                it.copy(sourceUrl = value.let { if (it.isBlank()) null else it })
            }
        }

    var favoriteCanteens: Set<Int>
        get() = prefs.getStringSet(KEY_FAVOURITES, emptySet())!!.map { it.toInt() }.toSet()
        set(value) {
            prefs.edit()
                    .putStringSet(KEY_FAVOURITES, value.map { it.toString() }.toSet())
                    .apply()

            settingsFlowInternal.update {
                it.copy(favoriteCanteenIds = value)
            }
        }

    var lastCanteenListUpdate: Long
        get() = prefs.getLong(KEY_LAST_CANTEEN_LIST_UPDATE, 0)
        set(value) {
            prefs.edit()
                    .putLong(KEY_LAST_CANTEEN_LIST_UPDATE, value)
                    .apply()
        }

    var lastSelectedCanteenId: Int?
        get() = if (prefs.contains(KEY_LAST_SELECTED_CANTEEN_ID))
            prefs.getInt(KEY_LAST_SELECTED_CANTEEN_ID, -1)
        else
            null
        set(value) {
            prefs.edit()
                    .let { editor ->
                        if (value != null) {
                            editor.putInt(KEY_LAST_SELECTED_CANTEEN_ID, value)
                        } else {
                            editor.remove(KEY_LAST_SELECTED_CANTEEN_ID)
                        }

                        editor.apply()
                    }

            settingsFlowInternal.update {
                it.copy(lastSelectedCanteenId = value)
            }
        }

    private var selectedCities: List<String>
        get() = ArrayStringUtil.parse(prefs.getString(SELECTED_CITIES, null))
        set(value) {
            prefs.edit()
                    .putString(SELECTED_CITIES, ArrayStringUtil.serialize(value))
                    .apply()

            settingsFlowInternal.update {
                it.copy(cityHistory = value)
            }
        }

    fun saveSelectedCity(name: String) {
        selectedCities = selectedCities.toMutableList().apply {
            remove(name)
            add(0, name)
        }
    }

    init {
        // eventually delete old source url
        if (sourceUrl == DefaultApiUrl.UNSAFE_URL && !DefaultApiUrl.NEEDS_UNSAFE_URL) {
            if (!prefs.getBoolean(DID_MIGRATE_TO_SAFE_URL, false)) {
                prefs.edit()
                        .putBoolean(DID_MIGRATE_TO_SAFE_URL, true)
                        .putString(KEY_SOURCE_URL, DefaultApiUrl.SAFE_URL)
                        .apply()
            }
        }

        val lastAppVersion = prefs.getInt(KEY_LAST_APP_VERSION, 0)
        val currentAppVersion = BuildConfig.VERSION_CODE

        if (lastAppVersion != currentAppVersion) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "did update => wipe canteen list cache")
            }

            val edit = prefs.edit()
                    .putInt(KEY_LAST_APP_VERSION, currentAppVersion)
                    .remove(KEY_LAST_CANTEEN_LIST_UPDATE)

            if (lastAppVersion < 21) {
                if (sourceUrl == DefaultApiUrl.SAFE_URL && DefaultApiUrl.NEEDS_UNSAFE_URL) {
                    // require new confirmation
                    edit.remove(KEY_SOURCE_URL)
                }
            }

            edit.apply()
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "no update")
            }
        }
    }
}
