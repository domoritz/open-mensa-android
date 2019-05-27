package de.uni_potsdam.hpi.openmensa.ui.citylist.full


import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.uni_potsdam.hpi.openmensa.databinding.FullCityListDialogFragmentBinding
import de.uni_potsdam.hpi.openmensa.extension.addTextChangeListener
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils

class FullCityListDialogFragment : DialogFragment() {
    companion object {
        private const val DIALOG_TAG = "FullCityListDialogFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val model = ViewModelProviders.of(this).get(FullCityListDialogModel::class.java)
        val binding = FullCityListDialogFragmentBinding.inflate(inflater, container, false)
        val settings = SettingsUtils.with(context!!)

        val adapter = FullCityListAdapter()

        adapter.listener = object: AdapterListener {
            override fun onCityClicked(cityName: String) {
                settings.selectCity(cityName)
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
                dismiss()
            }
        }

        binding.filter.addTextChangeListener { model.termLive.value = binding.filter.text.toString() }
        model.termLive.value = binding.filter.text.toString()

        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.adapter = adapter

        model.filteredCityNames.observe(this, Observer {
            adapter.content = it
            binding.isEmpty = it.isEmpty()
        })

        settings.selectedCityLive.observe(this, Observer {
            adapter.selectedCityName = it
        })

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply {
        setOnShowListener {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, DIALOG_TAG)
}
