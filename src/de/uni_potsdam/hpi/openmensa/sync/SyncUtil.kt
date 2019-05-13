package de.uni_potsdam.hpi.openmensa.sync

import de.uni_potsdam.hpi.openmensa.api.client.ApiClient
import de.uni_potsdam.hpi.openmensa.api.client.queryAllItems
import de.uni_potsdam.hpi.openmensa.data.AppDatabase

object SyncUtil {
    fun syncCanteenList(api: ApiClient, database: AppDatabase) {
        val canteens = api.canteens.queryAllItems()

        database.runInTransaction {
            database.canteen().insertOrReplace(canteens)
            database.canteen().deleteOldItems(currentItemIds = canteens.map { it.id })
        }
    }

    fun syncDays(api: ApiClient, database: AppDatabase, canteenId: Int) {
        val days = api.queryDays(canteenId = canteenId).queryAllItems()

        database.runInTransaction {
            database.day().insertOrReplace(days)
            database.day().deleteOldItems(canteenId = canteenId, currentDates = days.map { it.date })
        }
    }

    fun syncMeals(api: ApiClient, database: AppDatabase, canteenId: Int, date: String) {
        val meals = api.queryMeals(canteenId = canteenId, date = date).queryAllItems()

        database.runInTransaction {
            database.meal().insertOrReplace(meals)
            database.meal().deleteOldItems(canteenId = canteenId, date = date, currentItemIds = meals.map { it.id })
        }
    }
}