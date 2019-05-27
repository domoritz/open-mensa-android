package de.uni_potsdam.hpi.openmensa.ui.canteenlist.full


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
import de.uni_potsdam.hpi.openmensa.MainActivity
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.databinding.FullCanteenListDialogFragmentBinding
import de.uni_potsdam.hpi.openmensa.extension.addTextChangeListener

class FullCanteenListDialogFragment : DialogFragment() {
    companion object {
        private const val DIALOG_TAG = "FullCanteenListDialogFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FullCanteenListDialogFragmentBinding.inflate(inflater, container, false)
        val model = ViewModelProviders.of(this).get(FullCanteenListModel::class.java)
        val adapter = CanteenListAdapter()
        val activity = activity as MainActivity

        adapter.listener = object: AdapterListener {
            override fun onCanteenClicked(canteen: Canteen) {
                activity.model.currentlySelectedCanteenId.value = canteen.id
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
                dismiss()
            }
        }

        binding.filter.addTextChangeListener { model.searchTerm.value = binding.filter.text.toString() }
        model.searchTerm.value = binding.filter.text.toString()

        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.adapter = adapter

        model.listContent.observe(this, Observer {
            adapter.canteens = it
            binding.isEmpty = it.isEmpty()
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
