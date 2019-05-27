package de.uni_potsdam.hpi.openmensa.ui.canteenlist.full

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import kotlin.properties.Delegates

class CanteenListAdapter: RecyclerView.Adapter<ViewHolder>() {
    var canteens: List<Canteen>? by Delegates.observable(null as List<Canteen>?) {
        _, _, _ -> notifyDataSetChanged()
    }
    var listener: AdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = canteens?.size ?: 0

    override fun getItemId(position: Int): Long = canteens!![position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_list_item_1,
                    parent,
                    false
            ) as TextView
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = canteens!![position]

        holder.view.text = item.name
        holder.view.setOnClickListener { listener?.onCanteenClicked(item) }
    }
}

class ViewHolder(val view: TextView): RecyclerView.ViewHolder(view)

interface AdapterListener {
    fun onCanteenClicked(canteen: Canteen)
}