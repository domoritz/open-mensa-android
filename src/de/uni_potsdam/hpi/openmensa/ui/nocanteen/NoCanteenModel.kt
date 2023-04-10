package de.uni_potsdam.hpi.openmensa.ui.nocanteen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.extension.map
import de.uni_potsdam.hpi.openmensa.extension.switchMap
import de.uni_potsdam.hpi.openmensa.sync.CanteenSyncing

class NoCanteenModel(application: Application): AndroidViewModel(application) {
    private val canteenListLength = AppDatabase.with(application).canteen.countItems()

    val status: LiveData<NoCanteenStatus> = CanteenSyncing.isWorking.switchMap { isWorking ->
        if (isWorking)
            MutableLiveData<NoCanteenStatus>().apply { value = NoCanteenStatus.Working }
        else
            canteenListLength.map {
                if (it == 0L)
                    NoCanteenStatus.NoData
                else
                    NoCanteenStatus.NoFavorites
            }
    }
}

enum class NoCanteenStatus {
    Working,
    NoData,
    NoFavorites
}