package de.uni_potsdam.hpi.openmensa.ui.canteenlist.full

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils

class FullCanteenListModel (application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val settings = SettingsUtils.with(application)

    val searchTerm = MutableLiveData<String>().apply { value = "" }

    private val fullCanteenList = settings.selectedCityLive.switchMap { city ->
        if (city != null)
            database.canteen().getByCity(city)
        else
            MutableLiveData<List<Canteen>>().apply { value = emptyList() }
    }

    val listContent = fullCanteenList.switchMap { list ->
        searchTerm.map { term ->
            list.filter { it.name.contains(term, ignoreCase = true) }
        }
    }
}