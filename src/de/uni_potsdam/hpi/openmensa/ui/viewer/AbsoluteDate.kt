package de.uni_potsdam.hpi.openmensa.ui.viewer

import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import java.text.DateFormat
import java.text.ParseException
import java.util.TimeZone

object AbsoluteDate {
    fun format(rawDate: String): String = try {
        DateFormat.getDateInstance(DateFormat.FULL).let { dateFormat ->
            dateFormat.timeZone = TimeZone.getDefault()
            dateFormat.format(DateUtils.parseToLocalTimezone(rawDate))
        }
    } catch (ex: ParseException) {
        rawDate
    }
}