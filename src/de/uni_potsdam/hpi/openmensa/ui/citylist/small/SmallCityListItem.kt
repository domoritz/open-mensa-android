package de.uni_potsdam.hpi.openmensa.ui.citylist.small

sealed class SmallCityListItem
data class CityListItem(val city: String, val reason: CityListItemReason): SmallCityListItem()
object CityListShowAll: SmallCityListItem()
object RequestLocationPermission: SmallCityListItem()

enum class CityListItemReason {
    Distance,
    History
}