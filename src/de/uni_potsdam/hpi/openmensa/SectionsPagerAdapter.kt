package de.uni_potsdam.hpi.openmensa

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.util.Log

import java.util.ArrayList

import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment
import kotlin.properties.Delegates

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the primary sections of the app.
 */
class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    var dates: List<String>? by Delegates.observable(null as List<String>?) {
        _, _, _ -> notifyDataSetChanged()
    }

    val daySections: ArrayList<Int>
        get() {
            val sections = ArrayList<Int>()
            for (i in 1 until count) {
                sections.add(i)
            }
            return sections
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

    fun setToFetching(on: Boolean, animated: Boolean) {
        // TODO: remove this
    }

    companion object {

        internal val NUM_ITEMS = 5
    }
}