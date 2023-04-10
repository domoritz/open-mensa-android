package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import de.uni_potsdam.hpi.openmensa.ui.day.DayFragment
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

    /**
     * Creates/ returns an Item
     */
    override fun getItem(position: Int): Fragment = DayFragment.newInstance(position)

    override fun getCount(): Int {
        return dates?.size ?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val itemDate = if (position < (dates?.size ?: 0)) dates!![position] else ""

        return RelativeDate.format(currentDate = currentDate, date = itemDate).uppercase()
    }
}