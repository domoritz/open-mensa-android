package de.uni_potsdam.hpi.openmensa.api.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap

class SettingsModel(application: Application): AndroidViewModel(application) {
    private val favoriteCanteenIds = SettingsUtils.getFavoriteCanteenIdsLive(application)
    private val favoriteCanteens = favoriteCanteenIds.switchMap { ids ->
        AppDatabase.with(application).canteen().getByIds(ids.toList())
    }
    val favoriteCanteensCounter = favoriteCanteens.map { it.size }
}