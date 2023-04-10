package de.uni_potsdam.hpi.openmensa.ui.citylist.small

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.citylist.full.FullCityListDialogFragment

class SmallCityListDialogFragment: BottomSheetDialogFragment() {
    companion object {
        const val DIALOG_TAG = "SmallCityListDialogFragment"
        private const val REQUEST_FULL_LIST = 1
        private const val REQUEST_LOCATION_ACCESS = 2
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!, SettingsUtils.with(context!!).selectedBottomSheetThemeTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recycler = RecyclerView(context!!)
        val model = ViewModelProviders.of(this).get(SmallCityListDialogModel::class.java)
        val adapter = SmallCityListAdapter()

        adapter.listener = object: AdapterListener {
            override fun onCityClicked(city: String) {
                SettingsUtils.with(context!!).selectCity(city)
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
                dismiss()
            }

            override fun onMoreClicked() {
                FullCityListDialogFragment().apply {
                    setTargetFragment(this@SmallCityListDialogFragment, REQUEST_FULL_LIST)
                }.show(fragmentManager!!)
            }

            override fun onRequestLocationAccessClicked() {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_ACCESS)
            }
        }
        adapter.iconTint = SettingsUtils.with(context!!).selectedThemeIconColor(resources.configuration)

        recycler.layoutManager = LinearLayoutManager(context!!)
        recycler.adapter = adapter

        model.shortCityList.observe(this, Observer {
            adapter.content = it
        })

        return recycler
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_FULL_LIST && resultCode == Activity.RESULT_OK) {
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
            dismiss()
        }
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, DIALOG_TAG)
}