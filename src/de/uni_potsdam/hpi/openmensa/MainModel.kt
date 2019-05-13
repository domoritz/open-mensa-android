package de.uni_potsdam.hpi.openmensa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing

class MainModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val favoriteCanteenIds = SettingsUtils.getFavoriteCanteenIdsLive(application)

    val favoriteCanteens = favoriteCanteenIds.switchMap { ids ->
        database.canteen().getByIds(ids.toList())
    }

    // TODO: save selected canteen across app restarts
    val currentlySelectedCanteenId = MutableLiveData<Int?>().apply { value = null }
    val currentlySelectedCanteen = currentlySelectedCanteenId.switchMap { id ->
        if (id != null)
            CanteenWithDays.with(database, id)
        else
            MutableLiveData<CanteenWithDays?>().apply { value = null }
    }
    val syncStatus = currentlySelectedCanteenId.switchMap { id ->
        MealSyncing.status.map { status ->
            if (id != null)
                status[id]
            else
                null
        }
    }

    fun refresh() {
        currentlySelectedCanteenId.value?.let { id ->
            MealSyncing.syncInBackground(canteenId = id, force = false, context = getApplication())
        }
    }

    fun confirmSyncStatus() {
        currentlySelectedCanteenId.value?.let { id ->
            MealSyncing.removeDoneStatus(id)
        }
    }
}

data class CanteenWithDays(
        val canteen: Canteen,
        val days: List<Day>
) {
    companion object {
        fun with(database: AppDatabase, canteenId: Int): LiveData<CanteenWithDays?> {
            val canteenLive = database.canteen().getById(canteenId)
            val daysLive = database.day().getByCanteenId(canteenId)

            return canteenLive.switchMap { canteen ->
                daysLive.map { days ->
                    if (canteen != null) {
                        CanteenWithDays(canteen, days)
                    } else {
                        null
                    }
                }
            }
        }
    }
}