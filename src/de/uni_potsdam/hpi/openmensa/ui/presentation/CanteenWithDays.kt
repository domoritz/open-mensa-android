package de.uni_potsdam.hpi.openmensa.ui.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import java.util.*

data class CanteenWithDays(
    val canteen: Canteen,
    val days: List<Day>
) {
    companion object {
        fun getLive(database: AppDatabase, canteenId: Int): LiveData<CanteenWithDays?> {
            val canteenLive = database.canteen.getById(canteenId)
            val daysLive = database.day.getByCanteenId(canteenId)

            return canteenLive.switchMap { canteen ->
                daysLive.map { days ->
                    if (canteen != null) CanteenWithDays(canteen, days)
                    else null
                }
            }
        }

        fun getSync(database: AppDatabase, canteenId: Int): CanteenWithDays? {
            val canteen = database.canteen.getByIdSync(canteenId)
            val days = database.day.getByCanteenIdSync(canteenId)

            return if (canteen != null) CanteenWithDays(canteen, days)
            else null
        }
    }

    fun getDatesToShow(currentDateString: String): List<String> {
        val canteenDays = days.sortedBy { it.date }

        return if (canteenDays.isEmpty()) {
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

            val firstDate =
                if (canteenDays.any { it.date == currentDateString }) currentDateString
                else if (isTodayNearBeforeNextDay) {
                    DateUtils.loadDateIntoCalendar(currentDateString, helpCalendar)
                    DateUtils.format(helpCalendar)
                } else canteenDays.first().date

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