package de.uni_potsdam.hpi.openmensa.ui.canteenlist.small

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.KnownLocationStatus
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.LocationUtil
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.MissingPermissionLocationStatus

// TODO: canteen map
class SmallCanteenListModel (application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val settings = SettingsUtils.with(application)
    private val selectedCity = settings.selectedCityLive
    private val allCanteens = selectedCity.switchMap { city ->
        if (city != null)
            database.canteen().getByCity(city)
        else
            MutableLiveData<List<Canteen>>().apply { value = emptyList() }
    }
    private val favoriteCanteenIds = settings.favoriteCanteensLive
    private val currentLocationLive = LocationUtil.getLocationLive(application)

    val shortList = favoriteCanteenIds.switchMap { favoriteIds ->
        allCanteens.switchMap { canteens ->
            currentLocationLive.map { location ->
                val result = mutableListOf<SmallCanteenListItem>()
                val src = canteens.toMutableList()

                // favorite canteens first
                favoriteIds.forEach { favoriteId ->
                    val canteen = src.find { it.id == favoriteId }

                    if (canteen != null) {
                        src.remove(canteen)
                        result.add(CanteenListItem(canteen, CanteenListItemReason.Favorite))
                    }
                }

                if (location is KnownLocationStatus) {
                    src.sortBy {
                        if (it.hasLocation) {
                            val canteenLocation = Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            }

                            canteenLocation.distanceTo(location.location)
                        } else {
                            Float.MAX_VALUE
                        }
                    }

                    // add up to 5 items by distance
                    val itemsByDistanceCounter = Math.max(3, 5 - result.size)
                    val itemsByDistance = src.subList(0, Math.min(itemsByDistanceCounter, src.size))
                            // required to clone the list because some android versions
                            // just return a wrapper for the original list (which is modified later)
                            .toMutableList()

                    result.addAll(itemsByDistance.map { CanteenListItem(it, CanteenListItemReason.Distance) })
                    src.removeAll(itemsByDistance)
                } else {
                    if (location is MissingPermissionLocationStatus) {
                        result.add(EnableLocationAccessItem)
                    }
                }

                if (src.isNotEmpty()) {
                    result.add(ShowAllItem)
                }

                result.add(SelectCityItem)

                result
            }
        }
    }

    val noCitySelected = selectedCity.map { it == null }
}