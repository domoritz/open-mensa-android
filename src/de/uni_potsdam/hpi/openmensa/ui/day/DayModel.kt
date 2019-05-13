package de.uni_potsdam.hpi.openmensa.ui.day

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.uni_potsdam.hpi.openmensa.MainModel
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import de.uni_potsdam.hpi.openmensa.extension.switchMap

class DayModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val activityViewModelLive = MutableLiveData<MainModel>()
    private val canteenIdLive = activityViewModelLive.switchMap {
        it?.currentlySelectedCanteenId ?: MutableLiveData<Int?>().apply { value = null }
    }
    val dateLive = MutableLiveData<String>()
    val meals = canteenIdLive.switchMap { canteenId ->
        if (canteenId == null) {
            MutableLiveData<List<Meal>>().apply { value = emptyList() }
        } else {
            dateLive.switchMap { date ->
                database.meal().getByCanteenAndDate(canteenId = canteenId, date = date)
            }
        }
    }

    fun init(activityViewModel: MainModel) {
        if (activityViewModelLive.value != activityViewModel) {
            activityViewModelLive.value = activityViewModel
        }
    }
}