package de.uni_potsdam.hpi.openmensa.ui.canteenlist


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import de.uni_potsdam.hpi.openmensa.api.Canteen
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.databinding.SelectCanteenDialogFragmentBinding
import de.uni_potsdam.hpi.openmensa.extension.addTextChangeListener
import de.uni_potsdam.hpi.openmensa.extension.toggle

// TODO: sort by location
class SelectCanteenDialogFragment : DialogFragment() {
    companion object {
        private const val STATE_SELECTED_CANTEENS = "selectedCanteens"
        private const val DIALOG_TAG = "SelectCanteenDialogFragment"
    }

    private val adapter = CanteenDialogFragmentAdapter()
    lateinit var selectedItems: MutableSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            selectedItems = SettingsUtils.getFavouriteCanteensFromPreferences(context!!).toMutableSet()
        } else {
            selectedItems = savedInstanceState.getStringArray(STATE_SELECTED_CANTEENS)!!.toMutableSet()
        }

        adapter.checkedItemIds = selectedItems
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putStringArray(STATE_SELECTED_CANTEENS, selectedItems.toTypedArray())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = SelectCanteenDialogFragmentBinding.inflate(LayoutInflater.from(context!!), null, false)
        val allCanteens = SettingsUtils.getStorage(context!!).canteens.values.toList()

        fun updateList() {
            val term = binding.filter.text.toString().trim()

            adapter.content = if (term.isEmpty()) {
                allCanteens
            } else {
                allCanteens.filter { it.name.contains(term, ignoreCase = true) }
            }
        }

        binding.list.layoutManager = LinearLayoutManager(context!!)
        binding.list.adapter = adapter

        binding.filter.addTextChangeListener { updateList() }
        updateList()

        adapter.listener = object: AdapterListener {
            override fun onCanteenClicked(canteen: Canteen) {
                selectedItems.toggle(canteen.key)
                adapter.notifyDataSetChanged()
            }
        }

        return AlertDialog.Builder(context!!)
                .setView(binding.root)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    SettingsUtils.setFavouriteCanteensAtPreferences(context!!, selectedItems)
                    SettingsUtils.updateFavouriteCanteensFromPreferences(context!!)
                }
                .setNeutralButton(android.R.string.cancel) { _, _ ->
                    // do not save
                }
                .create()
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, DIALOG_TAG)
}
