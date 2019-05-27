package de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist


import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
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
    lateinit var selectedItems: MutableSet<Int>
    lateinit var binding: SelectCanteenDialogFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            selectedItems = SettingsUtils.with(context!!).favoriteCanteens.toMutableSet()
        } else {
            selectedItems = savedInstanceState.getIntArray(STATE_SELECTED_CANTEENS)!!.toMutableSet()
        }

        adapter.checkedItemIds = selectedItems
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putIntArray(STATE_SELECTED_CANTEENS, selectedItems.toIntArray())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = SelectCanteenDialogFragmentBinding.inflate(LayoutInflater.from(context!!), null, false)
        val model = ViewModelProviders.of(this).get(SelectCanteenDialogModel::class.java)

        model.canteensSorted.observe(this, Observer {
            adapter.content = it
            binding.isEmpty = it.isEmpty()
        })

        model.missingLocation.observe(this, Observer {
            binding.missingLocation = it
        })

        binding.list.layoutManager = LinearLayoutManager(context!!)
        binding.list.adapter = adapter

        binding.filter.addTextChangeListener { model.termLive.value = binding.filter.text.toString() }
        model.termLive.value = binding.filter.text.toString()

        binding.sortByLocationCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (LocationUtil.hasLocationAccessPermission(context!!)) {
                    model.sortByDistanceLive.value = true
                } else {
                    buttonView.isChecked = false

                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_ACCESS)
                }
            } else {
                model.sortByDistanceLive.value = false
            }
        }
        model.sortByDistanceLive.value = binding.sortByLocationCheckbox.isChecked

        adapter.listener = object: AdapterListener {
            override fun onCanteenClicked(canteen: Canteen) {
                selectedItems.toggle(canteen.id)
                adapter.notifyDataSetChanged()
            }
        }

        return AlertDialog.Builder(context!!)
                .setView(binding.root)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    SettingsUtils.with(context!!).favoriteCanteens = selectedItems
                }
                .setNeutralButton(android.R.string.cancel) { _, _ ->
                    // do not save
                }
                .create()
                .apply {
                    setOnShowListener {
                        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    }
                }
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
