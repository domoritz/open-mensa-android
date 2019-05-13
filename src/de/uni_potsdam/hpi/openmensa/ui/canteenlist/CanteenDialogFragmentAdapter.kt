package de.uni_potsdam.hpi.openmensa.ui.canteenlist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import kotlin.properties.Delegates

class CanteenDialogFragmentAdapter: RecyclerView.Adapter<ViewHolder>() {
    var content: List<Canteen>? by Delegates.observable(null as List<Canteen>?) {
        _, _, _ -> notifyDataSetChanged()
    }

    var checkedItemIds: Set<Int> by Delegates.observable(emptySet()) {
        _, _, _ -> notifyDataSetChanged()
    }

    var listener: AdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = content?.size ?: 0

    override fun getItemId(position: Int): Long = content!![position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false) as CheckedTextView
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = content!![position]

        holder.view.text = item.name
        holder.view.isChecked = checkedItemIds.contains(item.id)
        holder.view.setOnClickListener {
            listener?.onCanteenClicked(item)
        }
    }
}

class ViewHolder(val view: CheckedTextView): RecyclerView.ViewHolder(view)

interface AdapterListener {
    fun onCanteenClicked(canteen: Canteen)
}