package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.presentation.CanteenWithDays
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest

class NewViewerModel(application: Application): AndroidViewModel(application), ViewerModelInterface {
    private val settings = SettingsUtils.with(application)
    private val database = AppDatabase.with(application)

    private val didAcceptPrivacy = settings.settingsFlow.map { it.sourceUrl != null }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentCanteenLive = settings.settingsFlow
        .map { it.lastSelectedCanteenId }
        .distinctUntilChanged()
        .transformLatest { canteenId ->
            if (canteenId == null) emit(null)
            else {
                val canteen = database.canteen.getByIdFlow(canteenId)
                val days = database.day.getByCanteenIdFlow(canteenId)
                val meals = database.meal.getByCanteenFlow(canteenId)

                val isFavoriteCanteen = settings.settingsFlow.mapLatest {
                    it.favoriteCanteenIds.contains(canteenId)
                }

                emitAll(combine(canteen, days, meals, isFavoriteCanteen) { a, b, c, d ->
                    if (a != null) Triple(CanteenWithDays(a, b), c, d)
                    else null
                })
            }
        }

    private val currentDate = DateUtils.localDate.asFlow()

    override val screen = channelFlow {
        send(ViewerModelInterface.Screen.Initializing)

        combine(didAcceptPrivacy, currentCanteenLive, currentDate) { didAcceptPrivacy, currentCanteen, currentDate ->
            if (!didAcceptPrivacy) ViewerModelInterface.Screen.Privacy
            else if (currentCanteen == null) ViewerModelInterface.Screen.WaitingForInitialCanteenSelection
            else {
                val (canteenDays, meals, isFavoriteCanteen) = currentCanteen
                val canteen = canteenDays.canteen
                val daysByDate = canteenDays.days.associateBy { it.date }
                val daysToShow = canteenDays.getDatesToShow(currentDate)

                ViewerModelInterface.Screen.Data(
                    currentCanteen = ViewerModelInterface.Screen.Data.CanteenInfo(canteen, isFavoriteCanteen),
                    days = daysToShow.map {
                        val date = daysByDate[it]

                        val content =
                            if (date == null) CanteenDay.Content.NoInformation
                            else if (date.closed) CanteenDay.Content.Closed
                            else CanteenDay.Content.Data(meals)

                        CanteenDay(
                            date = it,
                            content = content
                        )
                    }
                )
            }
        }.collectLatest { send(it) }
    }

    override fun addFavoriteCanteen(canteenId: Int) {
        settings.favoriteCanteens += canteenId
    }

    override fun removeFavoriteCanteen(canteenId: Int) {
        settings.favoriteCanteens -= canteenId
    }
}