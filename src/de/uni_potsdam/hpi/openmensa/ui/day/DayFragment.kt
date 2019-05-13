package de.uni_potsdam.hpi.openmensa.ui.day

import java.util.ArrayList

import android.os.Bundle
import androidx.core.app.ExpandableListFragment
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.uni_potsdam.hpi.openmensa.MainActivity
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.api.Day
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import de.uni_potsdam.hpi.openmensa.helpers.RefreshableFragment

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
class DayFragment : ExpandableListFragment(), RefreshableFragment {
    companion object {
        private const val EXTRA_DATE = "date"

        fun newInstance(date: String) = DayFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_DATE, date)
            }
        }
    }

    private val model: DayModel by lazy {
        ViewModelProviders.of(this).get(DayModel::class.java)
    }

    private val listItems = ArrayList<Meal>()
    // TODO: send argument using an other way
    var date: String? = null
        set(value) {
            field = value

            model.dateLive.value = value
        }

    private var fetching: Boolean? = false
    var isListVisible = false
        private set

    internal var adapterOld: OldMealAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.init((activity as MainActivity).model)
        model.dateLive.value = arguments!!.getString(EXTRA_DATE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapterOld = OldMealAdapter(activity!!, R.layout.list_item, listItems)

        // Assign adapterOld to ListView
        listAdapter = adapterOld

        model.meals.observe(this, Observer {
            listItems.addAll(it)
            adapterOld!!.notifyDataSetChanged()
            setListShownNoAnimation(true)
        })
    }

    override fun onResume() {
        refresh()
        super.onResume()
    }

    override fun refresh() {
        // TODO: reimplement all special cases
        /*
        if (isDetached || !isAdded || date == null)
            return

        val canteen = MainActivity.storage.getCurrentCanteen() ?: return

        val day = canteen.getDay(date)

        if (day == null) {
            if (fetching!!) {
                setToFetching(true, false)
            } else {
                if (MainActivity.isOnline(MainActivity.appContext!!)) {
                    setToNoInformation()
                } else {
                    setToNotOnline()
                }

            }
            return
        }

        if (day.closed) {
            setToClosed()
            return
        }

        setMealList(day)
        */
    }

    fun setEmptyText(text: String) {
        if (view == null) {
            Log.w(MainActivity.TAG, "List not yet created.")
            return
        }
        Log.d(MainActivity.TAG, String.format("Set text %s day %s", text, date))
        super.setEmptyText(text)
    }

    /**
     * tell the fragment that the canteen is closed today
     */
    fun setToClosed() {
        setEmptyText(resources.getString(R.string.canteenclosed))
        setToFetching(false, true)
        isListVisible = false
    }

    /**
     * tell the fragment that there is no information available for today
     */
    fun setToNoInformation() {
        setEmptyText(resources.getString(R.string.noinfo))
        setToFetching(false, true)
        isListVisible = false
    }

    /**
     * tell the fragment that there we are currently not online
     */
    fun setToNotOnline() {
        setEmptyText(resources.getString(R.string.noconnection))
        setToFetching(false, true)
        isListVisible = false
    }

    /**
     * clear the list of items
     */
    fun clear() {
        if (listItems == null || adapterOld == null)
            return
        listItems.clear()
        adapterOld!!.notifyDataSetChanged()
    }

    fun setToFetching(on: Boolean, animated: Boolean) {
        fetching = on
        if (isDetached || !isAdded)
            return
        clear()
        if (animated) {
            setListShown(!on)
        } else {
            setListShownNoAnimation(!on)
        }
    }

    protected fun setMealList(day: Day) {
        if (listItems == null || adapterOld == null)
            return

        if (day.isNullObject) {
            setToNoInformation()
            Log.d(MainActivity.TAG, String.format("Null object for day %s", day.date))
            return
        }

        isListVisible = true
        date = day.date
        setToFetching(false, false)
        // listItems.addAll(day.getMeals())
        adapterOld!!.notifyDataSetChanged()
    }
}