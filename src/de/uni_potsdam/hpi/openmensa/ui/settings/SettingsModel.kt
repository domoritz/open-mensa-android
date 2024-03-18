package de.uni_potsdam.hpi.openmensa.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils

class SettingsModel(application: Application): AndroidViewModel(application) {
    private val favoriteCanteenIds = SettingsUtils.with(application).favoriteCanteensLive
    private val favoriteCanteens = favoriteCanteenIds.switchMap { ids ->
        AppDatabase.with(application).canteen.getByIds(ids.toList())
    }
    val favoriteCanteensCounter = favoriteCanteens.map { it.size }
}