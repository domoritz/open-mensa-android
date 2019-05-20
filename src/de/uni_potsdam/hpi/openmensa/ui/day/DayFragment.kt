package de.uni_potsdam.hpi.openmensa.ui.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.uni_potsdam.hpi.openmensa.MainActivity
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import de.uni_potsdam.hpi.openmensa.databinding.DayFragmentBinding
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap
import de.uni_potsdam.hpi.openmensa.extension.toggle

/**
 * A fragment representing a section of the app, that displays the Meals for
 * one Day.
 */
class DayFragment : Fragment() {
    companion object {
        private const val EXTRA_INDEX = "index"
        private const val STATE_EXPANDED_ITEMS = "expanded_items"
        private const val PAGE_LIST = 0
        private const val PAGE_NO_DATA = 1
        private const val PAGE_CLOSED = 2

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
    val expandedItems = MutableLiveData<Set<Int>>().apply { value = emptySet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.init((activity as MainActivity).model)
        model.indexLive.value = arguments!!.getInt(EXTRA_INDEX)

        if (savedInstanceState != null) {
            expandedItems.value = savedInstanceState.getIntArray(STATE_EXPANDED_ITEMS)!!.toSet()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putIntArray(STATE_EXPANDED_ITEMS, expandedItems.value!!.toIntArray())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DayFragmentBinding.inflate(inflater, container, false)

        adapter.listener = object: MealAdapterListener {
            override fun onItemClicked(meal: Meal) {
                expandedItems.value = expandedItems.value!!.toMutableSet().apply { toggle(meal.id) }
            }
        }

        model.meals.switchMap { meals ->
            expandedItems.map { items ->
                meals to items
            }
        }.observe(this, Observer { (meals, items) ->
            adapter.meals = meals
            adapter.expandedItemIds = items
        })

        model.dayMode.observe(this, Observer {
            binding.flipper.displayedChild = when (it!!) {
                DayMode.ShowList -> PAGE_LIST
                DayMode.NoInformation -> PAGE_NO_DATA
                DayMode.Closed -> PAGE_CLOSED
            }
        })

        binding.recycler.layoutManager = LinearLayoutManager(context!!)
        binding.recycler.adapter = adapter

        return binding.root
    }
}