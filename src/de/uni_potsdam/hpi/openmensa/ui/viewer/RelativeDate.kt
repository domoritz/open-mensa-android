package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.text.format.DateUtils
import java.text.ParseException
import java.util.*

object RelativeDate {
    private val today = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
    private val help = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

    fun format(currentDate: String, date: String): String = synchronized(this) {
        try {
            de.uni_potsdam.hpi.openmensa.helpers.DateUtils.loadDateIntoCalendar(currentDate, today)
            de.uni_potsdam.hpi.openmensa.helpers.DateUtils.loadDateIntoCalendar(date, help)

            DateUtils.getRelativeTimeSpanString(
                de.uni_potsdam.hpi.openmensa.helpers.DateUtils.parseToLocalTimezone(date),
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS,
                0
            ).toString()
        } catch (ex: ParseException) {
            date
        }
    }
}