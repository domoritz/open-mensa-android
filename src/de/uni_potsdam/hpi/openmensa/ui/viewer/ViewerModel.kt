package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.extension.toggle
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing
import de.uni_potsdam.hpi.openmensa.ui.presentation.CanteenWithDays

class ViewerModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val settings = SettingsUtils.with(application)

    val currentlySelectedCanteenId = object: MutableLiveData<Int?>() {
        override fun setValue(value: Int?) {
            super.setValue(value)

            refresh(force = false)
        }
    }

    init {
        currentlySelectedCanteenId.value = settings.lastSelectedCanteenId
    }

    val currentlySelectedCanteen = currentlySelectedCanteenId.switchMap { id ->
        if (id != null)
            CanteenWithDays.getLive(database, id)
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
    val currentDate = DateUtils.localDate
    val datesToShow = currentDate.switchMap { currentDateString ->
        currentlySelectedCanteen.map { canteen ->
            canteen?.getDatesToShow(currentDateString) ?: listOf(currentDateString)
        }
    }

    val isSelectedCanteenFavorite = settings.favoriteCanteensLive.switchMap { favorites ->
        currentlySelectedCanteenId.map { canteenId ->
            canteenId != null && favorites.contains(canteenId)
        }
    }

    fun refresh(force: Boolean) {
        currentlySelectedCanteenId.value?.let { id ->
            MealSyncing.syncInBackground(canteenId = id, force = force, context = getApplication())
        }
    }

    fun confirmSyncStatus() {
        currentlySelectedCanteenId.value?.let { id ->
            MealSyncing.removeDoneStatus(id)
        }
    }

    fun saveSelectedCanteenId() {
        settings.lastSelectedCanteenId = currentlySelectedCanteenId.value
    }

    fun toggleFavorite() {
        currentlySelectedCanteenId.value?.let { canteenId ->
            settings.favoriteCanteens = settings.favoriteCanteens.toMutableSet().apply {
                toggle(canteenId)
            }
        }
    }
}