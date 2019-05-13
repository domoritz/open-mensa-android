package de.uni_potsdam.hpi.openmensa

import android.content.Context
import android.text.format.DateUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import de.uni_potsdam.hpi.openmensa.ui.day.DayFragment
import java.text.ParseException
import java.util.*
import kotlin.properties.Delegates

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the primary sections of the app.
 */
class SectionsPagerAdapter(val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    var currentDate: String by Delegates.observable("") {
        _, _, _ -> notifyDataSetChanged()
    }
    var dates: List<String>? by Delegates.observable(null as List<String>?) {
        _, _, _ -> notifyDataSetChanged()
    }

    private val today = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
    private val help = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

    /**
     * Creates/ returns an Item
     */
    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            CanteenFragment()
        } else {
            DayFragment.newInstance(position - 1)
        }
    }

    override fun getCount(): Int {
        return (dates?.size ?: 0) + 1
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {
            context.getString(R.string.section_canteen)
        } else {
            val indexToRead = position - 1
            val itemDate = if (indexToRead < (dates?.size ?: 0)) dates!![indexToRead] else ""

            try {
                de.uni_potsdam.hpi.openmensa.helpers.DateUtils.loadDateIntoCalendar(currentDate, today)
                de.uni_potsdam.hpi.openmensa.helpers.DateUtils.loadDateIntoCalendar(itemDate, help)

                DateUtils.getRelativeTimeSpanString(
                        de.uni_potsdam.hpi.openmensa.helpers.DateUtils.parseToLocalTimezone(itemDate),
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS,
                        0
                ).toString()
            } catch (ex: ParseException) {
                ""
            }
        }.toUpperCase()
    }
}