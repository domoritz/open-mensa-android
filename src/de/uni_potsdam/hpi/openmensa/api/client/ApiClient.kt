package de.uni_potsdam.hpi.openmensa.api.client

import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.DayWithMeals

interface ApiClient {
    val canteens: PagedApi<Canteen>
    fun queryDaysWithMeals(canteenId: Int): PagedApi<DayWithMeals>
}