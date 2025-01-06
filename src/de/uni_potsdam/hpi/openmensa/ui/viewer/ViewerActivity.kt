package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.ui.Theme
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.CanteenListModel
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.CanteenListViews

import de.uni_potsdam.hpi.openmensa.ui.settings.SettingsActivity
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.Locale

class ViewerActivity : FragmentActivity() {
    companion object {
        const val EXTRA_CANTEEN_ID = "canteenId"
        const val EXTRA_DATE = "date"
        const val EXTRA_MEAL_ID = "mealId"
    }

    private var extraCanteenId: Int? = null
    private var extraDate: String? = null
    private var extraMealId: Int? = null

    private val model: ViewerModelInterface by viewModels<ViewerModel>()
    private val canteenListModel by viewModels<CanteenListModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_CANTEEN_ID)) extraCanteenId =
                savedInstanceState.getInt(EXTRA_CANTEEN_ID)

            if (savedInstanceState.containsKey(EXTRA_DATE)) extraDate =
                savedInstanceState.getString(EXTRA_DATE)

            if (savedInstanceState.containsKey(EXTRA_MEAL_ID)) extraMealId =
                savedInstanceState.getInt(EXTRA_MEAL_ID)
        } else if (intent != null) {
            if (intent.hasExtra(EXTRA_CANTEEN_ID)) extraCanteenId =
                intent.getIntExtra(EXTRA_CANTEEN_ID, -1)

            if (intent.hasExtra(EXTRA_DATE)) extraDate =
                intent.getStringExtra(EXTRA_DATE)

            if (intent.hasExtra(EXTRA_MEAL_ID)) extraMealId =
                intent.getIntExtra(EXTRA_MEAL_ID, -1)
        }

        // init canteen selection
        extraCanteenId?.let {
            model.selectCanteen(it)

            extraCanteenId = null
        }

        val locationAccessResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // ignore
        }

        lifecycleScope.launch {
            canteenListModel.activityCommandChannel.consumeEach { command ->
                when (command) {
                    is CanteenListModel.ActivityCommand.RequestLocationAccess -> {
                        locationAccessResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    is CanteenListModel.ActivityCommand.HandleCanteenSelection -> {
                        model.selectCanteen(command.canteenId)
                    }
                    is CanteenListModel.ActivityCommand.HandleCanteenSelectionCancellation -> {
                        // ignore
                    }
                }
            }
        }

        // update the widgets
        if (savedInstanceState == null) MealWidget.updateAppWidgets(this)

        enableEdgeToEdge()

        setContent {
            val status by model.screen.collectAsState()
            val listStatus by canteenListModel.screen.collectAsState()

            val canteenInfo = remember (status) {
                status.let { when (it) {
                    is ViewerModelInterface.Screen.Data -> Pair(
                        it.currentCanteen.database, it.currentCanteen.isFavorite
                    )
                    else -> null
                } }
            }

            var showCanteenInfoDialog by rememberSaveable { mutableStateOf(false) }

            val hasCanteen = canteenInfo != null

            Theme {
                Scaffold (
                    topBar = {
                        TopAppBar(
                            title = {
                                if (canteenInfo == null) Text(stringResource(R.string.app_name))
                                else {
                                    Row(
                                        Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                canteenListModel.startSelectionIfIdle()
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            canteenInfo.first.name,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = stringResource(R.string.canteen_list_open)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (canteenInfo != null) {
                                    val isFavorite = canteenInfo.second

                                    IconButton(
                                        onClick = {
                                            if (isFavorite) {
                                                model.removeFavoriteCanteen(canteenInfo.first.id)
                                            } else {
                                                model.addFavoriteCanteen(canteenInfo.first.id)
                                            }
                                        },
                                        content = {
                                            if (isFavorite) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = stringResource(R.string.menu_favorite_rm)
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.StarBorder,
                                                    contentDescription = stringResource(R.string.menu_favorite_add)
                                                )
                                            }
                                        }
                                    )
                                }

                                var dropdownMenuExpanded by remember { mutableStateOf(false) }

                                IconButton(
                                    onClick = {
                                        dropdownMenuExpanded = true
                                    },
                                    content = {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.menu_open)
                                        )
                                    }
                                )

                                DropdownMenu(
                                    expanded = dropdownMenuExpanded,
                                    onDismissRequest = { dropdownMenuExpanded = false },
                                    content = {
                                        if (hasCanteen) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.reload))
                                                },
                                                onClick = {
                                                    model.refresh()

                                                    dropdownMenuExpanded = false
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.canteen_info))
                                                },
                                                onClick = {
                                                    showCanteenInfoDialog = true
                                                    dropdownMenuExpanded = false
                                                }
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.menu_preferences))
                                            },
                                            onClick = {
                                                startActivity(Intent(this@ViewerActivity, SettingsActivity::class.java))

                                                dropdownMenuExpanded = false
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(model.snackbar)
                    }
                ) { insets ->
                    status.let { status ->
                        when (status) {
                            ViewerModelInterface.Screen.Initializing -> {
                                Box(Modifier.fillMaxSize().padding(insets).padding(16.dp)) {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                }
                            }

                            is ViewerModelInterface.Screen.Privacy -> {
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(insets)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.privacy_dialog_title),
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    Text(
                                        stringResource(
                                            R.string.privacy_dialog_text,
                                            status.serverUrl
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            model.acceptPrivacy(status.serverUrl)
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(stringResource(R.string.privacy_dialog_accept))
                                    }
                                }
                            }

                            ViewerModelInterface.Screen.LoadingCanteenListScreen -> {
                                Box(Modifier.fillMaxSize().padding(insets).padding(16.dp)) {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                }
                            }

                            ViewerModelInterface.Screen.NoCanteensKnownScreen -> {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(insets)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.setup_no_data_text),
                                        Modifier.fillMaxWidth().align(Alignment.Center),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            ViewerModelInterface.Screen.WaitingForInitialCanteenSelection -> {
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(insets)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                                ) {
                                    Text(
                                        stringResource(R.string.setup_no_selection_text),
                                        Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )

                                    Button(
                                        onClick = {
                                            canteenListModel.startSelectionIfIdle()
                                        },
                                        content = {
                                            Text(stringResource(R.string.setup_no_selection_action))
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }

                            is ViewerModelInterface.Screen.Data -> {
                                val pagerState = rememberPagerState(
                                    pageCount = { status.days.size },
                                    initialPage = extraDate?.let {
                                        val index = status.days.indexOfFirst { it.rawDate == extraDate }

                                        extraDate = null

                                        if (index >= 0) index
                                        else null
                                    } ?: 0
                                )

                                val pagerStateScope = rememberCoroutineScope()
                                var expandedItems by rememberSaveable { mutableStateOf(
                                    if (extraMealId == null) emptySet<Int>()
                                    else setOf(extraMealId).also { extraMealId = null }
                                ) }

                                Column(
                                    Modifier.padding(top = insets.calculateTopPadding())
                                ) {
                                    ScrollableTabRow(
                                        selectedTabIndex = pagerState.currentPage.coerceAtMost(status.days.size - 1)
                                    ) {
                                        status.days.forEachIndexed { index, day ->
                                            Tab(
                                                selected = pagerState.currentPage == index,
                                                onClick = {
                                                    pagerStateScope.launch {
                                                        pagerState.animateScrollToPage(index)
                                                    }
                                                },
                                                content = {
                                                    Text(
                                                        day.relativeDate,
                                                        Modifier.padding(8.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize(),
                                        pageContent = { index ->
                                            val page = status.days[index]

                                            when (page.content) {
                                                ViewerModelInterface.CanteenDay.Content.Closed -> Column(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .padding(bottom = insets.calculateBottomPadding())
                                                ) {
                                                    DayViews.DateHeader(page.absoluteDate)

                                                    DayViews.BigMessage(
                                                        stringResource(R.string.canteenclosed),
                                                        Modifier.fillMaxSize()
                                                    )
                                                }

                                                ViewerModelInterface.CanteenDay.Content.NoInformation -> Column(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .padding(bottom = insets.calculateBottomPadding())
                                                ) {
                                                    DayViews.DateHeader(page.absoluteDate)

                                                    DayViews.BigMessage(
                                                        stringResource(R.string.noinfo),
                                                        Modifier.fillMaxSize()
                                                    )
                                                }

                                                is ViewerModelInterface.CanteenDay.Content.Data -> {
                                                    LazyColumn (
                                                        contentPadding = PaddingValues(bottom = insets.calculateBottomPadding()),
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        item {
                                                            DayViews.DateHeader(page.absoluteDate)
                                                        }

                                                        items (page.content.meals) { item ->
                                                            when (item) {
                                                                is ViewerModelInterface.ListItem.CategoryHeader -> Text(
                                                                    item.label,
                                                                    style = MaterialTheme.typography.titleLarge,
                                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                                )
                                                                is ViewerModelInterface.ListItem.MealItem -> {
                                                                    val isExpanded = expandedItems.contains(item.meal.id)

                                                                    fun toggle() {
                                                                        if (isExpanded) expandedItems -= item.meal.id
                                                                        else expandedItems += item.meal.id
                                                                    }

                                                                    Column (
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clickable { toggle() }
                                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                                    ) {
                                                                        Text(item.meal.name, style = MaterialTheme.typography.bodyLarge)

                                                                        if (item.meal.notes.isNotEmpty())
                                                                            Text(
                                                                                item.meal.notes.joinToString(", "),
                                                                                style = MaterialTheme.typography.bodyMedium
                                                                            )

                                                                        AnimatedVisibility(isExpanded) {
                                                                            val items = remember(item.meal) {
                                                                                listOf(
                                                                                    Pair(R.string.students, item.meal.prices?.students),
                                                                                    Pair(R.string.employees, item.meal.prices?.students),
                                                                                    Pair(R.string.pupils, item.meal.prices?.pupils),
                                                                                    Pair(R.string.other, item.meal.prices?.others)
                                                                                ).filter { (it.second ?: 0.0) > 0.0 }
                                                                            }

                                                                            if (items.isEmpty()) {
                                                                                Text(
                                                                                    stringResource(R.string.no_known_price),
                                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                                    modifier = Modifier
                                                                                        .fillMaxWidth()
                                                                                        .padding(horizontal = 8.dp)
                                                                                )
                                                                            } else DayViews.TableLayout (
                                                                                Modifier
                                                                                    .fillMaxWidth()
                                                                                    .padding(horizontal = (32 - 8).dp)
                                                                            ) {
                                                                                for ((label, value) in items) {
                                                                                    Text(
                                                                                        stringResource(label),
                                                                                        style = MaterialTheme.typography.bodyLarge,
                                                                                        modifier = Modifier.padding(end = 16.dp)
                                                                                    )

                                                                                    Text(
                                                                                        String.format(
                                                                                            Locale.getDefault(), "%.2f", value),
                                                                                        style = MaterialTheme.typography.bodyLarge
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                CanteenListViews.View(listStatus)

                if (showCanteenInfoDialog && canteenInfo != null) AlertDialog(
                    onDismissRequest = { showCanteenInfoDialog = false },
                    title = { Text(canteenInfo.first.name) },
                    text = {
                        val canteen = canteenInfo.first

                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                canteen.address,
                                Modifier.weight(1.0f)
                            )

                            IconButton(
                                onClick =  {
                                    val latlon = "${canteen.latitude},${canteen.longitude}"

                                    val uri = Uri.Builder()
                                        .scheme("geo")
                                        .path(latlon)
                                        .appendQueryParameter("z", "18") // zoom
                                        .appendQueryParameter("q", latlon + "(" + canteen.name + ")")
                                        .build()

                                    try {
                                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(this@ViewerActivity, resources.getString(R.string.nomapapp), Toast.LENGTH_LONG).show()
                                    }
                                },
                                content = {
                                    Icon(
                                        Icons.Default.Map,
                                        stringResource(R.string.external_map_btn)
                                    )
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCanteenInfoDialog = false
                            },
                            content = {
                                Text(stringResource(android.R.string.ok))
                            }
                        )
                    },
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        extraCanteenId?.let { outState.putInt(EXTRA_CANTEEN_ID, it) }
        extraDate?.let { outState.putString(EXTRA_DATE, it) }
        extraMealId?.let { outState.putInt(EXTRA_MEAL_ID, it) }
    }
}
