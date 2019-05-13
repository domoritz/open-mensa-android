package de.uni_potsdam.hpi.openmensa.ui.day

import java.util.ArrayList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.uni_potsdam.hpi.openmensa.MainActivity
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import de.uni_potsdam.hpi.openmensa.databinding.DayFragmentBinding
import de.uni_potsdam.hpi.openmensa.extension.toggle

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
class DayFragment : Fragment() {
    companion object {
        private const val EXTRA_INDEX = "index"
        private const val STATE_EXPANDED_ITEMS = "expanded_items"

        fun newInstance(index: Int) = DayFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_INDEX, index)
            }
        }
    }

    private val model: DayModel by lazy {
        ViewModelProviders.of(this).get(DayModel::class.java)
    }

    val adapter = MealAdapter()
    val expandedItems = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.init((activity as MainActivity).model)
        model.indexLive.value = arguments!!.getInt(EXTRA_INDEX)

        if (savedInstanceState != null) {
            expandedItems.addAll(savedInstanceState.getIntArray(STATE_EXPANDED_ITEMS)!!.toList())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putIntArray(STATE_EXPANDED_ITEMS, expandedItems.toIntArray())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DayFragmentBinding.inflate(inflater, container, false)

        adapter.expandedItemIds = expandedItems
        adapter.listener = object: MealAdapterListener {
            override fun onItemClicked(meal: Meal) {
                expandedItems.toggle(meal.id)
                adapter.notifyDataSetChanged()
            }
        }

        model.meals.observe(this, Observer {
            adapter.meals = it
        })

        binding.recycler.layoutManager = LinearLayoutManager(context!!)
        binding.recycler.adapter = adapter

        return binding.root
    }

    // TODO: reimplement all of these again
    /*
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

override fun refresh() {
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
    */
}