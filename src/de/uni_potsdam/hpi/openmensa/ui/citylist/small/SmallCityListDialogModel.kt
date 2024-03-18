package de.uni_potsdam.hpi.openmensa.ui.citylist.small

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.KnownLocationStatus
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.LocationUtil
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.MissingPermissionLocationStatus

class SmallCityListDialogModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val settings = SettingsUtils.with(application)

    private val selectedCitiesHistory = settings.selectedCitiesLive
    private val location = LocationUtil.getLocationLive(application)
    private val canteensFromDatabase = database.canteen.getAll()

    private val citiesByDistance = canteensFromDatabase.switchMap { canteens ->
        location.map { loc ->
            if (loc is KnownLocationStatus)
                canteens.sortedBy { canteen ->
                    val canteenLocation = Location("").apply {
                        latitude = canteen.latitude
                        longitude = canteen.longitude
                    }

                    canteenLocation.distanceTo(loc.location)
                }
            else
                canteens
        }
    }.map { list -> list.map { it.city }.distinct() }

    val shortCityList = selectedCitiesHistory.switchMap { history ->
        location.switchMap { loc ->
            citiesByDistance.map { cities ->
                val result = mutableListOf<SmallCityListItem>()
                val src = cities.toMutableList()

                // take up to 3 by history
                history.forEach { historyItem ->
                    if (result.size < 3) {
                        if (src.remove(historyItem)) {
                            result.add(CityListItem(historyItem, CityListItemReason.History))
                        }
                    }
                }

                // take up to 5 by distance if location is known
                if (loc is KnownLocationStatus) {
                    val itemsByDistance = src.subList(0, Math.min(5, src.size))

                    result.addAll(itemsByDistance.map { CityListItem(it, CityListItemReason.Distance) })
                }

                if (loc is MissingPermissionLocationStatus) {
                    result.add(RequestLocationPermission)
                }

                result.add(CityListShowAll)

                result
            }
        }
    }
}