package de.uni_potsdam.hpi.openmensa.ui.day

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import de.uni_potsdam.hpi.openmensa.data.model.isEmpty
import de.uni_potsdam.hpi.openmensa.databinding.MealListItemBinding
import java.util.*
import kotlin.properties.Delegates

class MealAdapter: RecyclerView.Adapter<MealViewHolder>() {
    companion object {
        @JvmStatic
        fun formatPrice(double: Double?) = String.format(Locale.getDefault(), "%.2f", double)
    }

    var meals: List<Meal>? by Delegates.observable(null as List<Meal>?) { _, _, _ -> notifyDataSetChanged() }
    var expandedItemIds: Set<Int> by Delegates.observable(emptySet()) { _, _, _ -> notifyDataSetChanged() }
    var listener: MealAdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = meals?.size ?: 0

    override fun getItemId(position: Int): Long = meals!![position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MealViewHolder(
            MealListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
            )
    )

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val item = meals!![position]

        holder.view.apply {
            header.name = item.name
            header.category = item.category
            header.notes = item.notes.joinToString(", ")

            details.visible = expandedItemIds.contains(item.id)
            details.employeesPrice = item.prices?.students
            details.employeesPrice = item.prices?.employees
            details.pupilsPrice = item.prices?.pupils
            details.otherPrice = item.prices?.others
            details.noKnownPrice = item.prices.isEmpty()

            root.setOnClickListener {
                listener?.onItemClicked(item)
            }
        }
    }
}

class MealViewHolder(val view: MealListItemBinding): RecyclerView.ViewHolder(view.root)

interface MealAdapterListener {
    fun onItemClicked(meal: Meal)
}