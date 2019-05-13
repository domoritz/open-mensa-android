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
}