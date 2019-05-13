package de.uni_potsdam.hpi.openmensa

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import de.uni_potsdam.hpi.openmensa.ui.day.DayFragment
import kotlin.properties.Delegates

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the primary sections of the app.
 */
class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    var dates: List<String>? by Delegates.observable(null as List<String>?) {
        _, _, _ -> notifyDataSetChanged()
    }

    /**
     * Creates/ returns an Item
     */
    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            CanteenFragment()
        } else {
            DayFragment.newInstance(dates!![position - 1])
        }
    }

    override fun getCount(): Int {
        return (dates?.size ?: 0) + 1
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val context = MainActivity.appContext
        when (position) {
            0 -> return context!!.getString(R.string.section_canteen).toUpperCase()
            1 -> return context!!.getString(R.string.section_yesterday).toUpperCase()
            2 -> return context!!.getString(R.string.section_today).toUpperCase()
            3 -> return context!!.getString(R.string.section_tomorrow).toUpperCase()
            4 -> return context!!.getString(R.string.section_da_tomorrow).toUpperCase()
        }
        return null
    }
}