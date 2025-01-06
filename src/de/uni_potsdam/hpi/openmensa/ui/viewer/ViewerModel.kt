package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.app.Application
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.api.DefaultApiUrl
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.sync.CanteenSyncing
import de.uni_potsdam.hpi.openmensa.sync.MealSyncing
import de.uni_potsdam.hpi.openmensa.ui.presentation.CanteenWithDays
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.math.roundToLong

class ViewerModel(application: Application): AndroidViewModel(application), ViewerModelInterface {
    companion object {
        private const val LOG_TAG = "ViewerModel"
    }

    private val settings = SettingsUtils.with(application)
    private val database = AppDatabase.with(application)

    private val didAcceptPrivacy = settings.settingsFlow.map { it.sourceUrl != null }
        .distinctUntilChanged()

    private val currentCanteenId = settings.settingsFlow
        .map { it.lastSelectedCanteenId }
        .distinctUntilChanged()

    private val refreshChannel = Channel<Unit>(capacity = Channel.RENDEZVOUS)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentCanteenLive = currentCanteenId
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override val screen = didAcceptPrivacy.transformLatest {
        if (it) {
            coroutineScope {
                val localCanteenListRetryChannel = Channel<Unit>(Channel.RENDEZVOUS)
                val isSyncingLive = MutableStateFlow(true)
                val hasCanteensLive = database.canteen.countItems()
                    .map { it > 0 }
                    .distinctUntilChanged()

                launch {
                    var hasCanteens = hasCanteensLive.first()
                    var lastErrorJob: Job? = null

                    if (hasCanteens) {
                        val duration = 1000 * 10 + 1000 * 30 * Math.random()

                        if (BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, "delay initial canteen list sync by $duration ms")
                        }

                        isSyncingLive.value = false

                        delay(duration.roundToLong())
                    }

                    while (true) {
                        isSyncingLive.value = true

                        lastErrorJob?.cancel()

                        try {
                            CanteenSyncing.runSynchronousAndThrowEventually(false, getApplication())
                        } catch (ex: Exception) {
                            if (BuildConfig.DEBUG) {
                                Log.d(LOG_TAG, "could not sync canteen list", ex)
                            }

                            if (!hasCanteens) lastErrorJob = launch {
                                val result = snackbar.showSnackbar(
                                    getApplication<Application>().getString(R.string.sync_snackbar_failed),
                                    getApplication<Application>().getString(R.string.sync_snackbar_retry)
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    localCanteenListRetryChannel.trySend(Unit)
                                }
                            }
                        } finally {
                            isSyncingLive.value = false
                        }

                        select {
                            localCanteenListRetryChannel.onReceive {
                                if (BuildConfig.DEBUG) {
                                    Log.d(LOG_TAG, "user requested canteen sync list retry")
                                }
                            }
                            launch {
                                delay((1000 * 60 * 60 + 1000 * 300 * Math.random()).roundToLong())
                            }.onJoin {
                                if (BuildConfig.DEBUG) {
                                    Log.d(LOG_TAG, "hour has passed to retry canteen list sync")
                                }
                            }
                            if (hasCanteens) launch {
                                hasCanteensLive.first { it == false }
                            }.onJoin {
                                if (BuildConfig.DEBUG) {
                                    Log.d(LOG_TAG, "canteen list lost")
                                }

                                hasCanteens = false

                                Unit
                            } else launch {
                                hasCanteensLive.first { it == true }
                            }.onJoin {
                                if (BuildConfig.DEBUG) {
                                    Log.d(LOG_TAG, "external canteen list sync succeeded")
                                }

                                hasCanteens = true

                                Unit
                            }
                        }
                    }
                }

                launch {
                    currentCanteenId.transformLatest<_, Unit> { canteenId ->
                        coroutineScope {
                            val localRefreshChannel = Channel<Unit>(Channel.RENDEZVOUS)

                            var failedSyncCounter = 0
                            var lastMessageJob: Job? = null

                            if (canteenId != null) while (true) {
                                val force = select {
                                    refreshChannel.onReceive {
                                        if (BuildConfig.DEBUG) {
                                            Log.d(LOG_TAG, "refresh due to external user request")
                                        }

                                        true
                                    }
                                    localRefreshChannel.onReceive {
                                        if (BuildConfig.DEBUG) {
                                            Log.d(LOG_TAG, "refresh due to internal user request")
                                        }

                                        true
                                    }
                                    launch {
                                        if (failedSyncCounter > 0) {
                                            val backoff =
                                                1000 * 60 * Math.pow(
                                                    2.0,
                                                    failedSyncCounter.toDouble() - 1
                                                ) * (1 + Math.random())

                                            if (BuildConfig.DEBUG) {
                                                Log.d(
                                                    LOG_TAG,
                                                    "wait $backoff before next auto sync attempt"
                                                )
                                            }

                                            delay(backoff.roundToLong())
                                        }

                                        while (!MealSyncing.shouldSync(
                                                canteenId,
                                                getApplication()
                                            )
                                        ) {
                                            delay(1000 * 60)
                                        }
                                    }.onJoin {
                                        if (BuildConfig.DEBUG) {
                                            Log.d(LOG_TAG, "refresh due to outdated data")
                                        }

                                        false
                                    }
                                }

                                lastMessageJob?.cancel()

                                val startSnackbar = launch {
                                    snackbar.showSnackbar(getApplication<Application>().getString(R.string.sync_snackbar_running))
                                }

                                try {
                                    MealSyncing.syncCanteenSynchronousThrowEventually(
                                        canteenId = canteenId,
                                        force = force,
                                        context = getApplication()
                                    )

                                    failedSyncCounter = 0

                                    lastMessageJob = launch {
                                        snackbar.showSnackbar(
                                            getApplication<Application>().getString(
                                                R.string.sync_snackbar_done
                                            )
                                        )
                                    }
                                } catch (ex: Exception) {
                                    failedSyncCounter = (failedSyncCounter + 1).coerceAtMost(6)

                                    lastMessageJob = launch {
                                        val result = snackbar.showSnackbar(
                                            getApplication<Application>().getString(R.string.sync_snackbar_failed),
                                            getApplication<Application>().getString(R.string.sync_snackbar_retry)
                                        )

                                        if (result == SnackbarResult.ActionPerformed) {
                                            localRefreshChannel.trySend(Unit)
                                        }
                                    }
                                } finally {
                                    startSnackbar.cancel()
                                }
                            }
                        }
                    }.collect()
                }

                emitAll(
                    combine(currentCanteenLive, currentDate, isSyncingLive, hasCanteensLive) { currentCanteen, currentDate, isSyncing, hasCanteens ->
                        if (currentCanteen == null) {
                            if (hasCanteens) ViewerModelInterface.Screen.WaitingForInitialCanteenSelection
                            else if (isSyncing) ViewerModelInterface.Screen.LoadingCanteenListScreen
                            else ViewerModelInterface.Screen.NoCanteensKnownScreen
                        } else {
                            val (canteenDays, meals, isFavoriteCanteen) = currentCanteen
                            val canteen = canteenDays.canteen
                            val daysByDate = canteenDays.days.associateBy { it.date }
                            val daysToShow = canteenDays.getDatesToShow(currentDate)

                            ViewerModelInterface.Screen.Data(
                                currentCanteen = ViewerModelInterface.Screen.Data.CanteenInfo(canteen, isFavoriteCanteen),
                                days = daysToShow.map { rawDate ->
                                    val date = daysByDate[rawDate]

                                    val content =
                                        if (date == null) ViewerModelInterface.CanteenDay.Content.NoInformation
                                        else if (date.closed) ViewerModelInterface.CanteenDay.Content.Closed
                                        else {
                                            val dayMeals = meals.filter { it.date == date.date }
                                            val categories = dayMeals.map { it.category }.distinct()
                                            val mealsByCategory = dayMeals.groupBy { it.category }
                                            val result = mutableListOf<ViewerModelInterface.ListItem>()

                                            categories.forEach { category ->
                                                result.add(ViewerModelInterface.ListItem.CategoryHeader(category))

                                                result.addAll(
                                                    mealsByCategory[category]!!
                                                        .map { ViewerModelInterface.ListItem.MealItem(it) }
                                                )
                                            }

                                            ViewerModelInterface.CanteenDay.Content.Data(result)
                                        }

                                    ViewerModelInterface.CanteenDay(
                                        rawDate = rawDate,
                                        absoluteDate = AbsoluteDate.format(rawDate),
                                        relativeDate = RelativeDate.format(currentDate, rawDate),
                                        content = content
                                    )
                                }
                            )
                        }
                    }
                )
            }
        }
        else emit(ViewerModelInterface.Screen.Privacy(DefaultApiUrl.SAFE_URL))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), ViewerModelInterface.Screen.Initializing)

    override val snackbar = SnackbarHostState()

    override fun acceptPrivacy(serverUrl: String) {
        settings.sourceUrl = serverUrl
    }

    override fun selectCanteen(canteenId: Int) {
        settings.lastSelectedCanteenId = canteenId
    }

    override fun addFavoriteCanteen(canteenId: Int) {
        settings.favoriteCanteens += canteenId
    }

    override fun removeFavoriteCanteen(canteenId: Int) {
        settings.favoriteCanteens -= canteenId
    }

    override fun refresh() {
        refreshChannel.trySend(Unit)
    }
}