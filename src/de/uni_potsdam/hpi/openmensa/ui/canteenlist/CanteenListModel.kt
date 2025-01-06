package de.uni_potsdam.hpi.openmensa.ui.canteenlist

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.extension.whileTrue
import de.uni_potsdam.hpi.openmensa.helpers.HighPriorityDispatcher
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.withContext

class CanteenListModel(application: Application): AndroidViewModel(application) {
    private val database = AppDatabase.with(application)
    private val settings = SettingsUtils.with(application)
    private val state = MutableStateFlow<State>(State.Invisible)

    private val location = LocationUtil.getLocationFlow(application).distinctUntilChanged().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        UnknownLocationStatus
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(viewModelScope.newCoroutineContext(HighPriorityDispatcher))

    val activityCommandChannel = Channel<ActivityCommand>(Channel.RENDEZVOUS)

    val screen = flow {
        while (true) {
            when (val initialValue = state.value) {
                is State.Invisible -> {
                    emit(Screen.None)

                    state.takeWhile { it is State.Invisible }.collect()
                }
                is State.Launch.SwitchCanteen -> {
                    val currentSettings = settings.settingsFlow.value

                    val canteen = currentSettings.lastSelectedCanteenId?.let {
                        withContext(Dispatchers.IO) {
                            database.canteen.getByIdSync(it)
                        }
                    }

                    val city = canteen?.city ?: currentSettings.cityHistory.firstOrNull()

                    state.compareAndSet(
                        initialValue,
                        if (city == null) State.Dialog(
                            State.SmallCanteenList(""),
                            State.SmallCityList,
                            forceCitySelection = true
                        ) else State.Dialog(State.SmallCanteenList(city))
                    )
                }
                is State.Dialog -> {
                    val isValid = state.map { it is State.Dialog }
                    val stateFlow = state.transform { if (it is State.Dialog) emit(it) }.stateIn(scope)

                    emitAll(isValid.whileTrue(
                        produceDialog(
                            stateFlow = stateFlow,
                            updateStateFlow = { updater ->
                                state.update { oldState ->
                                    if (oldState is State.Dialog) updater(oldState)
                                    else oldState
                                }
                            },
                            goBack = { canteen ->
                                if (canteen == null) {
                                    val result = activityCommandChannel.trySend(
                                        ActivityCommand.HandleCanteenSelectionCancellation
                                    )

                                    if (result.isSuccess) {
                                        state.value = State.Invisible
                                    }
                                } else {
                                    val result = activityCommandChannel.trySend(
                                        ActivityCommand.HandleCanteenSelection(
                                            canteenId = canteen.id,
                                            city = canteen.city
                                        )
                                    )

                                    if (result.isSuccess) {
                                        settings.saveSelectedCity(canteen.city)

                                        state.value = State.Invisible
                                    }
                                }
                            }
                        )
                    ))
                }
            }
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(1000), Screen.None)

    fun startSelectionIfIdle() {
        state.compareAndSet(State.Invisible, State.Launch.SwitchCanteen)
    }

    private fun produceDialog(
        stateFlow: StateFlow<State.Dialog>,
        updateStateFlow: ((State.Dialog) -> State.Dialog) -> Unit,
        goBack: (Canteen?) -> Unit
    ): Flow<Screen.Dialog> = flow {
        val canteenListLive = produceCanteenList(
            stateFlow = stateFlow.map { it.canteenList }.stateIn(scope),
            updateStateFlow = { updater ->
                updateStateFlow { oldState ->
                    oldState.copy(canteenList = updater(oldState.canteenList))
                }
            },
            launchCityList = { forceSelection ->
                updateStateFlow { oldState ->
                    if (oldState.cityList == null) oldState.copy(
                        cityList = State.SmallCityList,
                        forceCitySelection = forceSelection
                    ) else oldState.copy(
                        forceCitySelection = oldState.forceCitySelection || forceSelection
                    )
                }
            },
            goBack = goBack
        )

        val cityListLive = flow<Screen.CityList?> {
            val hasCityList = stateFlow.map { it.cityList != null }.stateIn(scope)

            while (true) {
                if (!hasCityList.value) {
                    emit(null)

                    hasCityList.takeWhile { !it }.collect()
                }

                emitAll(hasCityList.whileTrue(produceCityList(
                    stateFlow = stateFlow.transform {
                        if (it.cityList != null) emit(it.cityList)
                    }.stateIn(scope),
                    updateStateFlow = { updater ->
                        updateStateFlow { oldState ->
                            if (oldState.cityList != null) oldState.copy(
                                cityList = updater(oldState.cityList)
                            ) else oldState
                        }
                    },
                    goBack = { newCity ->
                        if (stateFlow.value.forceCitySelection && newCity == null) goBack(null)
                        else updateStateFlow { oldState ->
                            if (newCity == null) oldState.copy(cityList = null)
                            else oldState.copy(
                                canteenList = State.SmallCanteenList(city = newCity),
                                cityList = null
                            )
                        }
                    }
                )))
            }
        }

        emitAll(combine(canteenListLive, cityListLive) { canteenList, cityList ->
            Screen.Dialog(canteenList, cityList)
        })
    }

    private fun produceCanteenList(
        stateFlow: StateFlow<State.CanteenList>,
        updateStateFlow: ((State.CanteenList) -> State.CanteenList) -> Unit,
        launchCityList: (Boolean) -> Unit,
        goBack: (Canteen?) -> Unit
    ): Flow<Screen.CanteenList> = flow {
        while (true) {
            when (val initialValue = stateFlow.value) {
                is State.SmallCanteenList -> {
                    val canteenListLive = database.canteen.getByCityFlow(initialValue.city)
                    val favoriteCanteensLive = settings.settingsFlow.map { it.favoriteCanteenIds }
                    val validStateLive = stateFlow.map { it == initialValue }

                    val actions = Screen.CanteenList.Actions(
                        pickCanteen = { goBack(it.canteen) },
                        showAllCanteens = { updateStateFlow { State.BigCanteenList(city = initialValue.city) } },
                        switchCity = { launchCityList(false) },
                        requestLocationAccess = { activityCommandChannel.trySend(ActivityCommand.RequestLocationAccess) },
                        updateSearchTerm = {/* nothing to do */},
                        cancel = { goBack(null) }
                    )

                    emitAll(validStateLive.whileTrue(
                        combine(location, canteenListLive, favoriteCanteensLive) { location, canteenList, favoriteCanteens ->
                            val items = mutableListOf<CanteenListItem>()
                            val src = canteenList.toMutableList()

                            if (src.isEmpty()) {
                                launchCityList(true)
                            }

                            // favorite canteens first
                            favoriteCanteens.forEach { favoriteId ->
                                val canteen = src.find { it.id == favoriteId }

                                if (canteen != null) {
                                    src.remove(canteen)
                                    items.add(CanteenListItem(canteen, CanteenReason.Favorite))
                                }
                            }

                            if (location is KnownLocationStatus) {
                                val srcNearby = src.filter { it.hasLocation }.sortedBy {
                                    val canteenLocation = Location("").apply {
                                        latitude = it.latitude
                                        longitude = it.longitude
                                    }

                                    canteenLocation.distanceTo(location.location)
                                }.take(3.coerceAtLeast(5 - items.size))

                                items.addAll(srcNearby.map { CanteenListItem(it, CanteenReason.Distance) })
                                src.removeAll(srcNearby)
                            }

                            Screen.CanteenList(
                                items = items,
                                mode = Screen.CanteenList.Small(
                                    isMissingLocationAccess = location is MissingPermissionLocationStatus,
                                    hasMoreCanteens = src.isNotEmpty(),
                                ),
                                actions = actions
                            )
                        }
                    ))
                }
                is State.BigCanteenList -> {
                    val canteenListLive = database.canteen.getByCityFlow(initialValue.city)
                    val favoriteCanteensLive = settings.settingsFlow.map { it.favoriteCanteenIds }
                    val validStateLive = stateFlow.map { it is State.BigCanteenList && it.city == initialValue.city }
                    val searchTermLive = stateFlow.transform<_, String> {
                        if (it is State.BigCanteenList && it.city == initialValue.city)
                            emit(it.searchTerm)
                    }

                    val actions = Screen.CanteenList.Actions(
                        pickCanteen = { goBack(it.canteen) },
                        showAllCanteens = {/* nothing to do */},
                        switchCity = { launchCityList(false) },
                        requestLocationAccess = {/* nothing to do */},
                        updateSearchTerm = { term ->
                            updateStateFlow { oldState ->
                                if (oldState is State.BigCanteenList && oldState.city == initialValue.city)
                                    oldState.copy(searchTerm = term)
                                else
                                    oldState
                            }
                        },
                        cancel = { goBack(null) }
                    )

                    emitAll(validStateLive.whileTrue(
                        combine(searchTermLive, canteenListLive, favoriteCanteensLive) { searchTerm, canteenList, favoriteCanteens ->
                            if (canteenList.isEmpty()) {
                                launchCityList(true)
                            }

                            val items = canteenList
                                .filter { it.name.contains(searchTerm, ignoreCase = true) }
                                .map { CanteenListItem(
                                    it,
                                    if (favoriteCanteens.contains(it.id)) CanteenReason.Favorite
                                    else CanteenReason.Listing
                                ) }

                            Screen.CanteenList(
                                items = items,
                                mode = Screen.CanteenList.Big(
                                    searchTerm = searchTerm
                                ),
                                actions = actions
                            )
                        }
                    ))
                }
            }
        }
    }

    private fun produceCityList(
        stateFlow: StateFlow<State.CityList>,
        updateStateFlow: ((State.CityList) -> State.CityList) -> Unit,
        goBack: (String?) -> Unit
    ): Flow<Screen.CityList> = flow {
        while (true) {
            emitAll(when (stateFlow.value) {
                is State.SmallCityList -> {
                    val historyLive = settings.settingsFlow.map { it.cityHistory }
                    val fullCanteenListLive = database.canteen.getAllFlow()

                    val citiesByDistanceLive =
                        combine(fullCanteenListLive, location) { canteens, loc ->
                            if (loc is KnownLocationStatus)
                                canteens.sortedBy { canteen ->
                                    val canteenLocation = Location("").apply {
                                        latitude = canteen.latitude
                                        longitude = canteen.longitude
                                    }

                                    canteenLocation.distanceTo(loc.location)
                                }
                            else
                                canteens
                        }.distinctUntilChanged().map { list -> list.map { it.city }.distinct() }

                    val isLocationKnownLive = location.map { it is KnownLocationStatus }.distinctUntilChanged()
                    val isLocationPermissionMissingLive = location.map { it is MissingPermissionLocationStatus }.distinctUntilChanged()

                    val actions = Screen.CityList.Actions(
                        pickCity = { goBack(it.city) },
                        requestLocationAccess = { activityCommandChannel.trySend(ActivityCommand.RequestLocationAccess) },
                        showAllCities = { updateStateFlow { State.BigCityList() } },
                        updateSearchTerm = {/* nothing to do */},
                        cancel = { goBack(null) }
                    )

                    val result = combine(historyLive, citiesByDistanceLive, isLocationKnownLive, isLocationPermissionMissingLive) { history, citiesByDistance, isLocationKnown, isLocationPermissionMissing ->
                        val list = mutableListOf<CityListItem>()
                        val src = citiesByDistance.toMutableList()

                        // take up to 3 by history
                        history.forEach { historyItem ->
                            if (list.size < 3) {
                                if (src.remove(historyItem)) {
                                    list.add(CityListItem(historyItem, CityReason.History))
                                }
                            }
                        }

                        // take up to 5 by distance if location is known
                        if (isLocationKnown) {
                            val itemsByDistance = src.subList(0, Math.min(5, src.size))

                            list.addAll(itemsByDistance.map { CityListItem(it, CityReason.Distance) })
                        }

                        Screen.CityList(
                            items = list,
                            mode = Screen.CityList.Small(
                                isMissingLocationAccess = isLocationPermissionMissing
                            ),
                            actions = actions
                        )
                    }

                    stateFlow.map { it is State.SmallCityList }.whileTrue(result)
                }
                is State.BigCityList -> {
                    val searchTermLive = stateFlow.transform {
                        if (it is State.BigCityList) emit(it.searchTerm)
                    }

                    val allCityNamesLive = database.canteenCity.getCitiesFlow().map { list -> list.map { it.city } }

                    val actions = Screen.CityList.Actions(
                        pickCity = { goBack(it.city) },
                        requestLocationAccess = {/* nothing to do */},
                        showAllCities = {/* nothing to do */},
                        updateSearchTerm = { searchTerm ->
                            updateStateFlow {
                                when (it) {
                                    is State.SmallCityList -> it
                                    is State.BigCityList -> it.copy(searchTerm = searchTerm)
                                }
                            }
                        },
                        cancel = { goBack(null) }
                    )

                    val result = combine(searchTermLive, allCityNamesLive) { searchTerm, cityNames ->
                        val items = cityNames
                            .filter { it.contains(searchTerm, ignoreCase = true) }
                            .map { CityListItem(it, CityReason.Listing) }

                        Screen.CityList(
                            items = items,
                            mode = Screen.CityList.Big(searchTerm = searchTerm),
                            actions = actions
                        )
                    }

                    stateFlow.map { it is State.BigCityList }.whileTrue(result)
                }
            })
        }
    }

    sealed class State {
        data object Invisible: State()

        sealed class Launch: State() {
            data object SwitchCanteen: Launch()
        }

        data class Dialog(
            val canteenList: CanteenList,
            val cityList: CityList? = null,
            val forceCitySelection: Boolean = false
        ): State()

        sealed class CanteenList

        data class SmallCanteenList(
            val city: String
        ): CanteenList()

        data class BigCanteenList(
            val city: String,
            val searchTerm: String = ""
        ): CanteenList()

        sealed class CityList()

        data object SmallCityList: CityList()

        data class BigCityList(
            val searchTerm: String = ""
        ): CityList()
    }

    sealed class Screen {
        data object None: Screen()

        data class Dialog(val canteen: CanteenList, val city: CityList?): Screen()

        data class CanteenList(
            val items: List<CanteenListItem>,
            val mode: Mode,
            val actions: Actions
        ) {
            sealed class Mode

            data class Small(
                val isMissingLocationAccess: Boolean,
                val hasMoreCanteens: Boolean
            ): Mode()

            data class Big(
                val searchTerm: String
            ): Mode()

            data class Actions(
                val pickCanteen: (CanteenListItem) -> Unit,
                val showAllCanteens: () -> Unit,
                val switchCity: () -> Unit,
                val requestLocationAccess: () -> Unit,
                val updateSearchTerm: (String) -> Unit,
                val cancel: () -> Unit
            )
        }

        data class CityList(
            val items: List<CityListItem>,
            val mode: Mode,
            val actions: Actions
        ) {
            sealed class Mode

            data class Small(val isMissingLocationAccess: Boolean): Mode()
            data class Big(val searchTerm: String): Mode()

            data class Actions(
                val pickCity: (CityListItem) -> Unit,
                val requestLocationAccess: () -> Unit,
                val showAllCities: () -> Unit,
                val updateSearchTerm: (String) -> Unit,
                val cancel: () -> Unit
            )
        }
    }

    sealed class ActivityCommand {
        data object RequestLocationAccess: ActivityCommand()
        data class HandleCanteenSelection(val city: String, val canteenId: Int): ActivityCommand()
        data object HandleCanteenSelectionCancellation: ActivityCommand()
    }

    data class CanteenListItem(val canteen: Canteen, val reason: CanteenReason)

    data class CityListItem(val city: String, val reason: CityReason)

    enum class CanteenReason {
        Favorite,
        Distance,
        Listing
    }

    enum class CityReason {
        Distance,
        History,
        Listing
    }
}