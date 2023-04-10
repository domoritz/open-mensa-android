package de.uni_potsdam.hpi.openmensa.sync

import de.uni_potsdam.hpi.openmensa.api.client.ApiClient
import de.uni_potsdam.hpi.openmensa.api.client.queryAllItems
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.CurrentCanteen
import de.uni_potsdam.hpi.openmensa.data.model.LastCanteenSync

object SyncUtil {
    fun syncCanteenList(api: ApiClient, database: AppDatabase) {
        val canteens = api.canteens.queryAllItems()

        database.runInTransaction {
            database.currentCanteen.deleteAllItems()

            database.canteen.update(canteens)
            database.canteen.insertOrIgnore(canteens)

            database.currentCanteen.insert(canteens.map { CurrentCanteen(id = it.id) })
            database.canteen.deleteOldItems()
        }
    }

    fun syncDaysAndMeals(api: ApiClient, database: AppDatabase, canteenId: Int) {
        val days = api.queryDaysWithMeals(canteenId = canteenId).queryAllItems()
        val meals = days.map { it.meals }.flatten()

        database.runInTransaction {
            database.day.insertOrReplace(days.map { it.toDay(canteenId) })
            database.day.deleteOldItems(canteenId = canteenId, currentDates = days.map { it.date })

            database.meal.insertOrReplace(meals)
            database.meal.deleteOldItems(canteenId = canteenId, currentItemIds = meals.map { it.id })

            database.lastCanteenSync.insertOrReplace(LastCanteenSync(
                    canteenId = canteenId,
                    timestamp = System.currentTimeMillis()
            ))
        }
    }
}