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
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import java.text.DateFormat
import java.text.ParseException
import java.util.*

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
        model.indexLive.value = requireArguments().getInt(EXTRA_INDEX)

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

        val mealList = model.meals.switchMap { meals ->
            expandedItems.map { items ->
                val categories = meals.map { it.category }.distinct()
                val mealsByCategory = meals.groupBy { it.category }

                val list = mutableListOf<MealItem>()

                categories.forEach { category ->
                    list.add(MealItem.Category(category))

                    val categoryMeals = mealsByCategory[category]!!

                    categoryMeals.forEach { meal ->
                        list.add(MealItem.ShortInfo(meal))

                        if (items.contains(meal.id)) {
                            list.add(MealItem.DetailInfo(meal))
                        }
                    }
                }

                list
            }
        }

        val dateHeaderText = model.dateLive.map {
            if (it != null) {
                try {
                    DateFormat.getDateInstance(DateFormat.FULL).let { dateFormat ->
                        dateFormat.timeZone = TimeZone.getDefault()
                        dateFormat.format(DateUtils.parseToLocalTimezone(it))
                    }
                } catch (ex: ParseException) {
                    null
                }
            } else null
        }

        val dateHeader = dateHeaderText.map {
            if (it != null) MealItem.Date(it) else null
        }

        val externalDateHeader = model.dayMode.switchMap { dayMode ->
            if (dayMode == DayMode.ShowList)
                MutableLiveData<String>().apply { value = null }
            else
                dateHeaderText
        }

        val fullMealList = dateHeader.switchMap { date ->
            mealList.map { meals ->
                if (date == null)
                    meals
                else
                    listOf(date) + meals
            }
        }

        fullMealList.observe(viewLifecycleOwner) { adapter.meals = it }

        model.dayMode.observe(viewLifecycleOwner) {
            binding.flipper.displayedChild = when (it!!) {
                DayMode.ShowList -> PAGE_LIST
                DayMode.NoInformation -> PAGE_NO_DATA
                DayMode.Closed -> PAGE_CLOSED
            }
        }

        externalDateHeader.observe(viewLifecycleOwner) {
            binding.date.date = it
        }

        binding.recycler.layoutManager = LinearLayoutManager(context!!)
        binding.recycler.adapter = adapter

        return binding.root
    }
}