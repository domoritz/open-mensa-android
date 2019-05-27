package de.uni_potsdam.hpi.openmensa.ui.canteenlist.small

import de.uni_potsdam.hpi.openmensa.data.model.Canteen

sealed class SmallCanteenListItem
data class CanteenListItem(val canteen: Canteen, val reason: CanteenListItemReason): SmallCanteenListItem()
object ShowAllItem: SmallCanteenListItem()
object SelectCityItem: SmallCanteenListItem()
object EnableLocationAccessItem: SmallCanteenListItem()

enum class CanteenListItemReason {
    Favorite, Distance
}