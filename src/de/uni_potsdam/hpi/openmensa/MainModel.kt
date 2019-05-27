package de.uni_potsdam.hpi.openmensa

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap
import de.uni_potsdam.hpi.openmensa.extension.toggle
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing
import java.util.*

class MainModel(application: Application): AndroidViewModel(application) {
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
    val currentDate = DateUtils.localDate
    val datesToShow = currentDate.switchMap { currentDateString ->
        currentlySelectedCanteen.map { canteen ->
            val canteenDays = (canteen?.days ?: emptyList()).sortedBy { it.date }

            if (canteenDays.isEmpty()) {
                // only today as fallback
                listOf(currentDateString)
            } else {
                val helpCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

                val isTodayNearBeforeNextDay = run {
                    DateUtils.loadDateIntoCalendar(currentDateString, helpCalendar)

                    repeat(4) {
                        if (DateUtils.format(helpCalendar) == canteenDays.first().date) {
                            return@run true
                        }

                        helpCalendar.add(Calendar.DATE, 1)
                    }

                    return@run false
                }

                val firstDate = if (isTodayNearBeforeNextDay) {
                    DateUtils.loadDateIntoCalendar(currentDateString, helpCalendar)
                    DateUtils.format(helpCalendar)
                } else
                    canteenDays.first().date

                val lastDate = canteenDays.last().date
                val dateList = mutableListOf<String>()

                DateUtils.loadDateIntoCalendar(firstDate, helpCalendar)

                while (dateList.size < 14 /* to cancel in case of malformed dates */) {
                    dateList.add(DateUtils.format(helpCalendar))

                    if (dateList.last() >= lastDate) {
                        break
                    }

                    helpCalendar.add(Calendar.DATE, 1)
                }

                dateList
            }
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