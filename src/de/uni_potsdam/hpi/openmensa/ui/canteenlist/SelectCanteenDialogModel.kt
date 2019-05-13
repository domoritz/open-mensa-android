package de.uni_potsdam.hpi.openmensa.ui.canteenlist

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap

class SelectCanteenDialogModel(application: Application): AndroidViewModel(application) {
    private val locationLive = LocationUtil.getLocationLive(application)
    private val allCanteensLive = AppDatabase.with(application).canteen().getAll()

    val termLive = MutableLiveData<String>().apply { value = "" }
    val sortByDistanceLive = MutableLiveData<Boolean>().apply { value = false }

    private val locationToUse = sortByDistanceLive.switchMap { sort ->
        if (sort)
            locationLive
        else
            MutableLiveData<LocationStatus>().apply { value = UnknownLocationStatus }
    }

    private val canteensFilteredBySearchTerm = allCanteensLive.switchMap { canteens ->
        termLive.map { term ->
            if (term.isEmpty()) {
                canteens
            } else {
                canteens.filter { it.name.contains(term, ignoreCase = true) || it.city.contains(term, ignoreCase = true) }
            }
        }
    }

    val missingLocation = locationToUse.switchMap { location ->
        sortByDistanceLive.map { sortByDistance ->
            location is UnknownLocationStatus && sortByDistance
        }
    }

    val canteensSorted = canteensFilteredBySearchTerm.switchMap { canteens ->
        locationToUse.map { location ->
            if (location is KnownLocationStatus) {
                canteens
                        .filter { it.hasLocation }
                        .sortedBy { canteen ->
                            val canteenLocation = Location("").apply {
                                latitude = canteen.latitude
                                longitude = canteen.longitude
                            }

                            canteenLocation.distanceTo(location.location)
                        }
            } else {
                canteens.sortedBy { it.name }
            }
        }
    }

}