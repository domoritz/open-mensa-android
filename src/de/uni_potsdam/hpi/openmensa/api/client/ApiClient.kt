package de.uni_potsdam.hpi.openmensa.api.client

import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.data.model.Meal

interface ApiClient {
    val canteens: PagedApi<Canteen>
    fun queryDays(canteenId: Int): PagedApi<Day>
    fun queryMeals(canteenId: Int, date: String): PagedApi<Meal>
}