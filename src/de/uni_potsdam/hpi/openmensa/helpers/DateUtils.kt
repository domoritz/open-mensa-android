package de.uni_potsdam.hpi.openmensa.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun loadDateIntoCalendar(date: String, calendar: Calendar) {
        dateFormat.timeZone = calendar.timeZone
        calendar.timeInMillis = dateFormat.parse(date).time
    }

    fun parseToLocalTimezone(date: String): Long {
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.parse(date).time
    }

    fun formatWithLocalTimezone(timestamp: Long): String {
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(Date(timestamp))
    }

    fun format(calendar: Calendar): String {
        dateFormat.timeZone = calendar.timeZone
        return dateFormat.format(Date(calendar.timeInMillis))
    }
}