package de.uni_potsdam.hpi.openmensa.ui.citylist.full

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap

class FullCityListDialogModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    val termLive = MutableLiveData<String>().apply { value = "" }
    private val allCityNames = database.canteenCity().getCities().map { list -> list.map { it.city } }

    val filteredCityNames = allCityNames.switchMap { cityNames ->
        termLive.map { term ->
            cityNames.filter { it.contains(term, ignoreCase = true) }
        }
    }
}