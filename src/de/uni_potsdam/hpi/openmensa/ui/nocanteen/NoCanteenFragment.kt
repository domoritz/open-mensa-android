package de.uni_potsdam.hpi.openmensa.ui.nocanteen


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.uni_potsdam.hpi.openmensa.databinding.NoCanteenFragmentBinding
import de.uni_potsdam.hpi.openmensa.sync.CanteenSyncing
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.small.SmallCanteenListDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist.SelectCanteenDialogFragment

class NoCanteenFragment : Fragment() {
    companion object {
        private const val PAGE_LOADING = 0
        private const val PAGE_NO_DATA = 1
        private const val PAGE_NO_FAVORITES = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = NoCanteenFragmentBinding.inflate(inflater, container, false)
        val model = ViewModelProviders.of(this).get(NoCanteenModel::class.java)

        model.status.observe(this, Observer {
            binding.flipper.displayedChild = when (it!!) {
                NoCanteenStatus.Working -> PAGE_LOADING
                NoCanteenStatus.NoData -> PAGE_NO_DATA
                NoCanteenStatus.NoFavorites -> PAGE_NO_FAVORITES
            }
        })

        binding.noSelection.selectCanteenButton.setOnClickListener {
            SmallCanteenListDialogFragment().show(fragmentManager!!)
        }

        binding.noData.retryButton.setOnClickListener {
            CanteenSyncing.runBackgroundSync(context!!)
        }

        return binding.root
    }
}
