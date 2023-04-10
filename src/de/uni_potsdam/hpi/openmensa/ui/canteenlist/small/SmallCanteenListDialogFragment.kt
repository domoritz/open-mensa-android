package de.uni_potsdam.hpi.openmensa.ui.canteenlist.small


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
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.full.FullCanteenListDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.citylist.small.SmallCityListDialogFragment

class SmallCanteenListDialogFragment : BottomSheetDialogFragment() {
    companion object {
        private const val DIALOG_TAG = "SmallCanteenListDialogFragment"
        private const val EXTRA_REQUEST_KEY = "request key"
        private const val REQUEST_FULL_CANTEEN_LIST = "full canteen list"
        private const val REQUEST_LOCATION_ACCESS = 2
        private const val REQUEST_SELECT_CITY_SETUP = 3

        const val RESULT_CANTEEN_ID = "canteenId"

        fun newInstance(requestKey: String) = SmallCanteenListDialogFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_REQUEST_KEY, requestKey)
            }
        }
    }

    private lateinit var requestKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!requireArguments().containsKey(EXTRA_REQUEST_KEY)) throw IllegalStateException()

        requestKey = requireArguments().getString(EXTRA_REQUEST_KEY, "")

        childFragmentManager.setFragmentResultListener(REQUEST_FULL_CANTEEN_LIST, this) { _, extras ->
            if (extras.containsKey(FullCanteenListDialogFragment.RESULT_CANTEEN_ID)) {
                dismissWithCanteenId(extras.getInt(FullCanteenListDialogFragment.RESULT_CANTEEN_ID))
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!, SettingsUtils.with(context!!).selectedBottomSheetThemeTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val model = ViewModelProviders.of(this).get(SmallCanteenListModel::class.java)
        val adapter = SmallCanteenListAdapter()
        val recycler = RecyclerView(context!!)

        adapter.listener = object: AdapterListener {
            override fun onCanteenClicked(canteen: Canteen) {
                dismissWithCanteenId(canteen.id)
            }

            override fun onMoreClicked() {
                FullCanteenListDialogFragment.newInstance(
                    REQUEST_FULL_CANTEEN_LIST
                ).show(childFragmentManager)
            }

            override fun onSelectCityClicked() {
                SmallCityListDialogFragment().show(fragmentManager!!)
            }

            override fun onEnableLocationAccessClicked() {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_ACCESS)
            }
        }
        adapter.iconTint = SettingsUtils.with(context!!).selectedThemeIconColor

        model.shortList.observe(this, Observer { adapter.content = it })
        model.noCitySelected.observe(this, Observer {
            if (it) {
                if (fragmentManager!!.findFragmentByTag(SmallCityListDialogFragment.DIALOG_TAG) == null) {
                    SmallCityListDialogFragment().apply {
                        setTargetFragment(this@SmallCanteenListDialogFragment, REQUEST_SELECT_CITY_SETUP)
                    }.show(fragmentManager!!)
                }
            }
        })

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        return recycler
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        parentFragmentManager.setFragmentResult(requestKey, Bundle())
    }

    private fun dismissWithCanteenId(canteenId: Int) {
        parentFragmentManager.setFragmentResult(requestKey, Bundle().apply {
            putInt(RESULT_CANTEEN_ID, canteenId)
        })

        dismiss()
    }

    private fun dismissWithoutResult() {
        parentFragmentManager.setFragmentResult(requestKey, Bundle())

        dismiss()
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, DIALOG_TAG)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECT_CITY_SETUP && resultCode != Activity.RESULT_OK) {
            dismissWithoutResult()
        }
    }
}
