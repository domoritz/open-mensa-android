package de.uni_potsdam.hpi.openmensa.helpers

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.Log
import de.uni_potsdam.hpi.openmensa.BuildConfig

import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.api.DefaultApiUrl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
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

        const val KEY_STYLE = "pref_style"
        private const val THEME_DARK = "dark"
        private const val THEME_LIGHT = "light"
        private const val THEME_AUTO = "auto"

        const val KEY_ENABLE_MAP = "pref_map"

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
        val favoriteCanteenIds: Set<Int>
    )

    private val settingsFlowInternal = MutableStateFlow(Settings(
        sourceUrl = sourceUrl.let { if (it.isBlank()) null else it },
        lastSelectedCanteenId = lastSelectedCanteenId,
        favoriteCanteenIds = favoriteCanteens
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

    private fun getThemeByString(theme: String): Int = when(theme) {
        THEME_DARK -> R.style.DarkAppTheme
        THEME_LIGHT -> R.style.LightAppTheme
        THEME_AUTO -> R.style.DayNightAppTheme
        else -> R.style.DayNightAppTheme
    }

    private fun getTranslucentTheme(theme: String): Int = when(theme) {
        THEME_DARK -> R.style.DarkTranslucentAppTheme
        THEME_LIGHT -> R.style.LightTranslucentAppTheme
        THEME_AUTO -> R.style.DayNightTranslucentAppTheme
        else -> R.style.DayNightTranslucentAppTheme
    }

    private fun getBottomSheetTheme(theme: String): Int = when(theme) {
        THEME_DARK -> R.style.DarkBottomSheetTheme
        THEME_LIGHT -> R.style.LightBottomSheetTheme
        THEME_AUTO -> R.style.DayNightBottomSheetTheme
        else -> R.style.DayNightBottomSheetTheme
    }

    private fun getIconColorByThemeByString(configuration: Configuration, theme: String): Int = when(theme) {
        THEME_DARK -> Color.WHITE
        THEME_LIGHT -> Color.BLACK
        THEME_AUTO -> if (isUsingOsNightMode(configuration)) Color.WHITE else Color.BLACK
        else -> if (isUsingOsNightMode(configuration)) Color.WHITE else Color.BLACK
    }

    private fun isUsingOsNightMode(configuration: Configuration) =
        configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private val selectedThemeName: String
        get() = prefs.getString(KEY_STYLE, THEME_LIGHT)!!

    val selectedTheme: Int
        get() = getThemeByString(selectedThemeName)

    val selectedTranslucentTheme: Int
        get() = getTranslucentTheme(selectedThemeName)

    val selectedBottomSheetThemeTheme: Int
        get() = getBottomSheetTheme(selectedThemeName)

    fun selectedThemeIconColor(configuration: Configuration): Int =
        getIconColorByThemeByString(configuration, selectedThemeName)

    val selectedThemeLive = LiveSettings.createObservablePreference(prefs, KEY_STYLE) { selectedTheme }
    val selectedTranslucentThemeLive = LiveSettings.createObservablePreference(prefs, KEY_STYLE) { selectedTranslucentTheme }

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

    val favoriteCanteensLive = LiveSettings.createObservablePreference(prefs, KEY_FAVOURITES) { favoriteCanteens }

    var lastCanteenListUpdate: Long
        get() = prefs.getLong(KEY_LAST_CANTEEN_LIST_UPDATE, 0)
        set(value) {
            prefs.edit()
                    .putLong(KEY_LAST_CANTEEN_LIST_UPDATE, value)
                    .apply()
        }

    var enableMap: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_MAP, false)
        set(value) {
            prefs.edit()
                    .putBoolean(KEY_ENABLE_MAP, value)
                    .apply()
        }

    val enableMapLive = LiveSettings.createObservablePreference(prefs, KEY_ENABLE_MAP) { enableMap }

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

    var selectedCities: List<String>
        get() = ArrayStringUtil.parse(prefs.getString(SELECTED_CITIES, null))
        set(value) {
            prefs.edit()
                    .putString(SELECTED_CITIES, ArrayStringUtil.serialize(value))
                    .apply()
        }

    val selectedCity: String?
        get() = selectedCities.firstOrNull()

    val selectedCityLive = LiveSettings.createObservablePreference(prefs, SELECTED_CITIES) { selectedCity }
    val selectedCitiesLive = LiveSettings.createObservablePreference(prefs, SELECTED_CITIES) { selectedCities }

    fun selectCity(name: String) {
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
