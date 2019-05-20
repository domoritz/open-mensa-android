package de.uni_potsdam.hpi.openmensa.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import de.uni_potsdam.hpi.openmensa.data.model.isEmpty
import de.uni_potsdam.hpi.openmensa.databinding.MealCategoryItemBinding
import de.uni_potsdam.hpi.openmensa.databinding.MealDetailsItemBinding
import de.uni_potsdam.hpi.openmensa.databinding.MealOverviewItemBinding
import java.util.*
import kotlin.properties.Delegates

class MealAdapter: RecyclerView.Adapter<MealViewHolder>() {
    companion object {
        @JvmStatic
        fun formatPrice(double: Double?) = String.format(Locale.getDefault(), "%.2f", double)

        private const val TYPE_DATE = 1
        private const val TYPE_CATEGORY = 2
        private const val TYPE_INFO = 3
        private const val TYPE_DETAIL = 4
    }

    var meals: List<MealItem>? by Delegates.observable(null as List<MealItem>?) { _, _, _ -> notifyDataSetChanged() }
    var listener: MealAdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = meals?.size ?: 0

    override fun getItemId(position: Int): Long = meals!![position].hashCode().toLong()

    override fun getItemViewType(position: Int): Int = when(meals!![position]) {
        is DateMealItem -> TYPE_DATE
        is MealCategoryItem -> TYPE_CATEGORY
        is MealShortInfoItem -> TYPE_INFO
        is MealDetailInfoItem -> TYPE_DETAIL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_DATE -> TODO()
        TYPE_CATEGORY -> MealCategoryHolder(
                MealCategoryItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
        TYPE_INFO -> MealInfoHolder(
                MealOverviewItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
        TYPE_DETAIL -> MealDetailHolder(
                MealDetailsItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
        else -> throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val item = meals!![position]

        when (item) {
            is DateMealItem -> TODO()
            is MealCategoryItem -> {
                holder as MealCategoryHolder

                holder.view.category = item.title
                holder.view.executePendingBindings()
            }
            is MealShortInfoItem -> {
                holder as MealInfoHolder
                val meal = item.meal

                holder.view.apply {
                    name = meal.name
                    notes = meal.notes.joinToString(", ")

                    linearLayout.setOnClickListener {
                        listener?.onItemClicked(meal)
                    }

                    executePendingBindings()
                }
            }
            is MealDetailInfoItem -> {
                val meal = item.meal
                val prices = meal.prices

                holder as MealDetailHolder

                holder.view.apply {
                    studentsPrice = prices?.students
                    employeesPrice = prices?.employees
                    pupilsPrice = prices?.pupils
                    otherPrice = prices?.others
                    noKnownPrice = prices.isEmpty()

                    linearLayout.setOnClickListener {
                        listener?.onItemClicked(meal)
                    }

                    executePendingBindings()
                }
            }
        }
    }
}

sealed class MealViewHolder(view: View): RecyclerView.ViewHolder(view)
class MealCategoryHolder(val view: MealCategoryItemBinding): MealViewHolder(view.root)
class MealInfoHolder(val view: MealOverviewItemBinding): MealViewHolder(view.root)
class MealDetailHolder(val view: MealDetailsItemBinding): MealViewHolder(view.root)

interface MealAdapterListener {
    fun onItemClicked(meal: Meal)
}