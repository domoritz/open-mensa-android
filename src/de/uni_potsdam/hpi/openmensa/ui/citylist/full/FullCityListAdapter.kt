package de.uni_potsdam.hpi.openmensa.ui.citylist.full

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class FullCityListAdapter: RecyclerView.Adapter<ViewHolder>() {
    var content: List<String>? by Delegates.observable(null as List<String>?) {
        _, _, _ -> notifyDataSetChanged()
    }
    var selectedCityName: String? by Delegates.observable(null as String?) {
        _, _, _ -> notifyDataSetChanged()
    }
    var listener: AdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = content?.size ?: 0

    override fun getItemId(position: Int): Long = content!![position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_single_choice, parent, false) as CheckedTextView
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = content!![position]

        holder.view.text = item
        holder.view.isChecked = item == selectedCityName
        holder.view.setOnClickListener {
            listener?.onCityClicked(item)
        }
    }
}

class ViewHolder(val view: CheckedTextView): RecyclerView.ViewHolder(view)

interface AdapterListener {
    fun onCityClicked(cityName: String)
}