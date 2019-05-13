package de.uni_potsdam.hpi.openmensa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.extension.switchMap

class MainModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val favoriteCanteenIds = SettingsUtils.getFavoriteCanteenIdsLive(application)

    val favoriteCanteens = favoriteCanteenIds.switchMap { ids ->
        database.canteen().getByIds(ids.toList())
    }

    val currentlySelectedCanteenId = MutableLiveData<Int?>().apply { value = null }
}