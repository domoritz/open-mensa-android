package de.uni_potsdam.hpi.openmensa.ui.day

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.uni_potsdam.hpi.openmensa.ui.viewer.ViewerModel
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Meal

class DayModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val activityViewModelLive = MutableLiveData<ViewerModel>()
    private val canteenIdLive = activityViewModelLive.switchMap {
        it?.currentlySelectedCanteenId ?: MutableLiveData<Int?>().apply { value = null }
    }
    val indexLive = MutableLiveData<Int>()
    val dateLive = activityViewModelLive.switchMap { model ->
        model.datesToShow.switchMap { dates ->
            indexLive.map { index ->
                if (dates.size > index)
                    dates[index]
                else
                    null
            }
        }
    }
    private val dayEntryLive = activityViewModelLive.switchMap { model ->
        dateLive.switchMap { date ->
            model.currentlySelectedCanteen.map { canteen ->
                canteen?.days?.find { it.date == date }
            }
        }
    }
    val meals = canteenIdLive.switchMap { canteenId ->
        if (canteenId == null) {
            MutableLiveData<List<Meal>>().apply { value = emptyList() }
        } else {
            dateLive.switchMap { date ->
                if (date != null) {
                    database.meal.getByCanteenAndDate(canteenId = canteenId, date = date)
                } else {
                    MutableLiveData<List<Meal>>().apply { value = emptyList() }
                }
            }
        }
    }
    val dayMode: LiveData<DayMode> = dayEntryLive.switchMap { dayEntry ->
        if (dayEntry == null) {
            MutableLiveData<DayMode>().apply { value = DayMode.NoInformation }
        } else if (dayEntry.closed) {
            MutableLiveData<DayMode>().apply { value = DayMode.Closed }
        } else {
            meals.map { meals ->
                if (meals.isEmpty()) {
                    DayMode.NoInformation
                } else {
                    DayMode.ShowList
                }
            }
        }
    }

    fun init(activityViewModel: ViewerModel) {
        if (activityViewModelLive.value != activityViewModel) {
            activityViewModelLive.value = activityViewModel
        }
    }
}

enum class DayMode {
    ShowList,
    NoInformation,
    Closed
}