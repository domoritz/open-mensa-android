package de.uni_potsdam.hpi.openmensa.ui.canteenlist


import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.api.Canteen
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.databinding.SelectCanteenDialogFragmentBinding
import de.uni_potsdam.hpi.openmensa.extension.addTextChangeListener
import de.uni_potsdam.hpi.openmensa.extension.toggle

class SelectCanteenDialogFragment : DialogFragment() {
    companion object {
        private const val STATE_SELECTED_CANTEENS = "selectedCanteens"
        private const val DIALOG_TAG = "SelectCanteenDialogFragment"
        private const val REQUEST_LOCATION_ACCESS = 1
    }

    private val adapter = CanteenDialogFragmentAdapter()
    lateinit var selectedItems: MutableSet<String>
    lateinit var binding: SelectCanteenDialogFragmentBinding

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
        binding = SelectCanteenDialogFragmentBinding.inflate(LayoutInflater.from(context!!), null, false)
        val allCanteens = SettingsUtils.getStorage(context!!).canteens.values.toList()

        fun updateList() {
            val term = binding.filter.text.toString().trim()
            val sortByDistance = binding.sortByLocationCheckbox.isChecked
            val location = LocationUtil.getLastBestLocation(context!!)

            if (location == MissingPermissionLocationStatus && sortByDistance) {
                binding.sortByLocationCheckbox.isChecked = false
            }

            val listContent = if (term.isEmpty()) {
                allCanteens
            } else {
                allCanteens.filter { it.name.contains(term, ignoreCase = true) }
            }

            val sortedList = if (sortByDistance && location is KnownLocationStatus) {
                listContent
                        // remove items with invalid coordinates
                        .filter {
                            it.coordinates.size == 2 &&
                                    it.coordinates[0] != null &&
                                    it.coordinates[1] != null
                        }
                        // sort by distance
                        .sortedBy { canteen ->
                            val canteenLocation = Location("").apply {
                                latitude = canteen.coordinates[0]!!.toDouble()
                                longitude = canteen.coordinates[1]!!.toDouble()
                            }

                            canteenLocation.distanceTo(location.location)
                        }
            } else {
                listContent.sortedBy { it.name }
            }

            adapter.content = sortedList
        }

        binding.list.layoutManager = LinearLayoutManager(context!!)
        binding.list.adapter = adapter

        binding.filter.addTextChangeListener { updateList() }
        binding.sortByLocationCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (LocationUtil.hasLocationAccessPermission(context!!)) {
                    updateList()
                } else {
                    buttonView.isChecked = false

                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_ACCESS)
                }
            } else {
                updateList()
            }
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_ACCESS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.sortByLocationCheckbox.isChecked = true
            } else {
                Toast.makeText(context!!, R.string.canteen_choose_sort_by_loc_rejected_toast, Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, DIALOG_TAG)
}
