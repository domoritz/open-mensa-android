package de.uni_potsdam.hpi.openmensa.ui.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMaxBy
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.extension.toggle
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.ui.viewer.ViewerActivity
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

        fun newInstance(index: Int) = DayFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_INDEX, index)
            }
        }
    }

    private val model: DayModel by lazy {
        ViewModelProviders.of(this).get(DayModel::class.java)
    }

    val expandedItems = MutableLiveData<Set<Int>>().apply { value = emptySet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewer = activity as ViewerActivity

        model.init(viewer.model)
        model.indexLive.value = requireArguments().getInt(EXTRA_INDEX)

        if (savedInstanceState != null) {
            expandedItems.value = savedInstanceState.getIntArray(STATE_EXPANDED_ITEMS)!!.toSet()
        } else {
            viewer.initialMealId?.let { expandedItems.value = setOf(it) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putIntArray(STATE_EXPANDED_ITEMS, expandedItems.value!!.toIntArray())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val dateHeaderText = dateHeaderText.observeAsState()
                val dayMode by model.dayMode.observeAsState(DayMode.ShowList)
                val mealList = mealList.observeAsState(emptyList<MealItem>())

                MaterialTheme {
                    when (dayMode) {
                        DayMode.ShowList -> LazyColumn {
                            item {
                                dateHeaderText.value?.let { DateHeader(it) }
                            }

                            items(mealList.value) { item ->
                                when (item) {
                                    is MealItem.Category -> Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                    is MealItem.ShortInfo -> Column (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedItems.value = expandedItems.value!!.toMutableSet().apply {
                                                    toggle(item.meal.id)
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(item.meal.name, style = MaterialTheme.typography.bodyLarge)

                                        if (item.meal.notes.isNotEmpty())
                                            Text(
                                                item.meal.notes.joinToString(", "),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                    }
                                    is MealItem.DetailInfo -> {
                                        val items = listOf(
                                            Pair(R.string.students, item.meal.prices?.students),
                                            Pair(R.string.employees, item.meal.prices?.students),
                                            Pair(R.string.pupils, item.meal.prices?.pupils),
                                            Pair(R.string.other, item.meal.prices?.others)
                                        ).filter { (it.second ?: 0.0) > 0.0 }

                                        if (items.isEmpty()) {
                                            Text(
                                                stringResource(R.string.no_known_price),
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedItems.value = expandedItems.value!!.toMutableSet().apply {
                                                            toggle(item.meal.id)
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp)
                                            )
                                        } else TableLayout (
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedItems.value = expandedItems.value!!.toMutableSet().apply {
                                                        toggle(item.meal.id)
                                                    }
                                                }
                                                .padding(horizontal = 32.dp)
                                        ) {
                                            for ((label, value) in items) {
                                                Text(
                                                    stringResource(label),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.padding(end = 16.dp)
                                                )

                                                Text(
                                                    String.format(Locale.getDefault(), "%.2f", value),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        DayMode.NoInformation -> Column (Modifier.fillMaxSize()) {
                            dateHeaderText.value?.let { DateHeader(it) }

                            BigMessage(stringResource(R.string.noinfo), Modifier.fillMaxSize())
                        }
                        DayMode.Closed -> Column (Modifier.fillMaxSize()) {
                            dateHeaderText.value?.let { DateHeader(it) }

                            BigMessage(stringResource(R.string.canteenclosed), Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DateHeader(text: String) {
        Column (
            Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    @Composable
    fun BigMessage(text: String, modifier: Modifier = Modifier) {
        Box (modifier.padding(16.dp)) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    @Composable
    fun TableLayout(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Layout(modifier = modifier, content = content) { measurables, constraints ->
            val labels = measurables.filterIndexed { index, measurable -> index % 2 == 0 }
            val values = measurables.filterIndexed { index, measurable -> index % 2 == 1 }

            val labelConstraints =
                if(constraints.hasBoundedWidth) Constraints(maxWidth = constraints.maxWidth * 2 / 3)
                else Constraints()

            val labelMeasure = labels.map {
                it.measure(labelConstraints)
            }

            val labelMaxWidth = labelMeasure.fastMaxBy { it.width }?.width ?: 0

            val valueConstraints =
                if(constraints.hasBoundedWidth) Constraints(maxWidth = (constraints.maxWidth - labelMaxWidth).coerceAtLeast(0))
                else Constraints()

            val valueMeasure = values.map {
                it.measure(valueConstraints)
            }

            val totalWidth =
                if (constraints.hasBoundedWidth) constraints.maxWidth
                else labelMaxWidth + (valueMeasure.fastMaxBy { it.width }?.width ?: 0)

            val totalHeight = labelMeasure.zip(valueMeasure).sumOf { (label, value) ->
                label.height.coerceAtLeast(value.height)
            }

            layout(totalWidth, totalHeight) {
                var yStart = 0

                labelMeasure.zip(valueMeasure).forEachIndexed { index, (label, value) ->
                    val height = label.height.coerceAtLeast(value.height)

                    label.placeRelative(0, yStart + (height - label.height) / 2)
                    value.placeRelative(labelMaxWidth, yStart + (height - value.height) / 2)

                    yStart += height
                }
            }
        }
    }
}