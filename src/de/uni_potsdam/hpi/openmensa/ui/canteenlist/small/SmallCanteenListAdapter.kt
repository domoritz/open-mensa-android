package de.uni_potsdam.hpi.openmensa.ui.canteenlist.small

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.databinding.BottomSheetListItemBinding
import kotlin.properties.Delegates

class SmallCanteenListAdapter: RecyclerView.Adapter<ViewHolder>() {
    var content: List<SmallCanteenListItem>? by Delegates.observable(null as List<SmallCanteenListItem>?) {
        _, _, _ -> notifyDataSetChanged()
    }
    var iconTint: Int? by Delegates.observable(null as Int?) { _, _, _ -> notifyDataSetChanged() }
    var listener: AdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = content?.size ?: 0

    override fun getItemId(position: Int): Long {
        val item = content!![position]

        return if (item is CanteenListItem) {
            item.canteen.id.toLong()
        } else {
            item.hashCode().toLong()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            BottomSheetListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
            )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = content!![position]

        val iconResource: Int
        val iconTint = iconTint

        when (item) {
            is CanteenListItem -> {
                holder.binding.text.text = item.canteen.name
                iconResource = when (item.reason) {
                    CanteenListItemReason.Distance -> R.drawable.ic_location_on_black_24dp
                    CanteenListItemReason.Favorite -> R.drawable.ic_star_black_24dp
                }
                holder.binding.root.setOnClickListener { listener?.onCanteenClicked(item.canteen) }
            }
            ShowAllItem -> {
                holder.binding.text.setText(R.string.canteen_list_show_more)
                iconResource = R.drawable.ic_unfold_more_black_24dp
                holder.binding.root.setOnClickListener { listener?.onMoreClicked() }
            }
            SelectCityItem -> {
                holder.binding.text.setText(R.string.canteen_list_select_city)
                iconResource = R.drawable.ic_location_city_black_24dp
                holder.binding.root.setOnClickListener { listener?.onSelectCityClicked() }
            }
            EnableLocationAccessItem -> {
                holder.binding.text.setText(R.string.canteen_list_enable_loc_access)
                iconResource = R.drawable.ic_location_on_black_24dp
                holder.binding.root.setOnClickListener { listener?.onEnableLocationAccessClicked() }
            }
            else -> throw IllegalArgumentException()
        }

        if (iconTint != null) {
            holder.binding.icon.setImageDrawable(
                    DrawableCompat.wrap(ContextCompat.getDrawable(holder.binding.icon.context, iconResource)!!).apply {
                        DrawableCompat.setTint(this, iconTint)
                    }
            )
        } else {
            holder.binding.icon.setImageResource(iconResource)
        }
    }
}

class ViewHolder(val binding: BottomSheetListItemBinding): RecyclerView.ViewHolder(binding.root)

interface AdapterListener {
    fun onCanteenClicked(canteen: Canteen)
    fun onMoreClicked()
    fun onSelectCityClicked()
    fun onEnableLocationAccessClicked()
}