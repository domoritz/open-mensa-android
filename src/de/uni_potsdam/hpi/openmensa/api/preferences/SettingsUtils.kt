package de.uni_potsdam.hpi.openmensa.api.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

import com.google.gson.Gson
import de.uni_potsdam.hpi.openmensa.BuildConfig

import de.uni_potsdam.hpi.openmensa.MainActivity
import de.uni_potsdam.hpi.openmensa.R

/**
 * Provides simple methods to access shared settings.
 *
 * @author dominik
 */
object SettingsUtils {
    const val KEY_SOURCE_URL = "pref_source_url"
    private const val KEY_STORAGE = "om_storage"

    // Make sure to update xml/preferences.xml as well
    const val KEY_FAVOURITES = "pref_favourites"

    const val KEY_STYLE = "pref_style"
    private const val THEME_DARK = "dark"
    private const val THEME_LIGHT = "light"

    private val gson = Gson()


    private fun getSharedPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getSourceUrl(context: Context): String = getSharedPrefs(context).getString(KEY_SOURCE_URL, context.resources.getString(R.string.source_url_default))!!

    fun getStorage(context: Context): Storage {
        // Throws ClassCastException if there is a preference with this name that is not a Set.
        val json = getSharedPrefs(context).getString(KEY_STORAGE, "{}")
        return gson.fromJson(json, Storage::class.java)
    }

    fun setStorage(context: Context, storage: Storage) {
        val json = gson.toJson(storage)
        //Log.d("json", json);

        getSharedPrefs(context).edit()
                .putString(KEY_STORAGE, json)
                .commit()
    }

    private fun getSelectedThemeName(context: Context) = getSharedPrefs(context).getString(KEY_STYLE, THEME_LIGHT)

    private fun getThemeByString(theme: String): Int = when(theme) {
        THEME_DARK -> R.style.DarkAppTheme
        THEME_LIGHT -> R.style.LightAppTheme
        else -> R.style.LightAppTheme
    }

    fun getSelectedTheme(context: Context) = getThemeByString(getSelectedThemeName(context))

    /**
     * saves the active canteens from the preferences in the storage object
     * @param context Application Context
     */
    fun updateFavouriteCanteensFromPreferences(context: Context) {
        val favouriteCanteenKeys = getFavouriteCanteensFromPreferences(context)

        if (BuildConfig.DEBUG) {
            Log.d(MainActivity.TAG, String.format("Got favourites %s", favouriteCanteenKeys))
        }

        val storage = getStorage(context)
        storage.setFavouriteCanteens(favouriteCanteenKeys.map { it.toString() }.toSet())
        storage.saveToPreferences(context)
    }

    fun getFavouriteCanteensFromPreferences(context: Context): Set<Int> {
        return getSharedPrefs(context).getStringSet(KEY_FAVOURITES, emptySet())!!.map { it.toInt() }.toSet()
    }

    fun setFavouriteCanteensAtPreferences(context: Context, canteenIds: Set<Int>) {
        getSharedPrefs(context).edit()
                .putStringSet(KEY_FAVOURITES, canteenIds.map { it.toString() }.toSet())
                .apply()
    }
}
