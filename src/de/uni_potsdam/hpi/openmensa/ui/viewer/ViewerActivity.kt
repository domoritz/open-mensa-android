package de.uni_potsdam.hpi.openmensa.ui.viewer

import android.content.Intent
import android.os.Bundle

import android.view.*
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.databinding.ActivityMainBinding

import de.uni_potsdam.hpi.openmensa.ui.settings.SettingsActivity
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.sync.*
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.small.SmallCanteenListDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.privacy.PrivacyDialogFragment
import de.uni_potsdam.hpi.openmensa.ui.nocanteen.NoCanteenFragment
import de.uni_potsdam.hpi.openmensa.ui.widget.MealWidget

class ViewerActivity : FragmentActivity() {
    companion object {
        const val EXTRA_CANTEEN_ID = "canteenId"
        const val EXTRA_DATE = "date"
        const val EXTRA_MEAL_ID = "mealId"
        private const val REQUEST_SELECT_CANTEEN = "select canteen"
    }

    private var isFirstResume = true
    private var extraCanteenId: Int? = null
    private var extraDate: String? = null
    private var extraMealId: Int? = null

    val initialMealId: Int?
        get() = extraMealId

    val model: ViewerModel by lazy {
        ViewModelProviders.of(this).get(ViewerModel::class.java)
    }

    val newModel: ViewerModelInterface by viewModels<NewViewerModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        val settings = SettingsUtils.with(this)
        val initialTheme = settings.selectedTheme
        var lastSnackbar: Snackbar? = null

        setTheme(initialTheme)
        super.onCreate(savedInstanceState)

        settings.selectedThemeLive.observe(this, Observer {
            if (it != initialTheme) recreate()
        })

        val binding = ActivityMainBinding.inflate(layoutInflater).also {
            // setContentView(it.root)
        }

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

        // setup toolbar

        // prepare setup screens
        if (savedInstanceState == null) {
            /*
            supportFragmentManager.beginTransaction()
                    .replace(R.id.no_canteen_container, NoCanteenFragment.newInstance(REQUEST_SELECT_CANTEEN))
                    .commit()
             */
        }

        PrivacyDialogFragment.showIfRequired(this)

        // setup pager
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        // binding.pager.adapter = sectionsPagerAdapter

        // binding.tabs.setupWithViewPager(binding.pager)

        model.currentDate.observe(this) { sectionsPagerAdapter.currentDate = it }
        model.datesToShow.observe(this) {
            sectionsPagerAdapter.dates = it

            if (extraDate != null) {
                it.indexOf(extraDate).let {
                    if (it != -1) binding.pager.setCurrentItem(it, false)
                }

                extraDate = null
            }
        }

        // init canteen selection
        if (extraCanteenId != null) {
            model.currentlySelectedCanteenId.value = extraCanteenId

            extraCanteenId = null
        }

        model.currentlySelectedCanteen.observe(this, Observer { currentCanteen ->
            binding.spinnerText.text = currentCanteen?.canteen?.name ?: ""

            val noCanteen = currentCanteen == null

            binding.flipper.displayedChild = if (noCanteen) 1 else 0
            binding.spinner.visibility = if (noCanteen) View.GONE else View.VISIBLE
            binding.tabs.visibility = if (noCanteen) View.GONE else View.VISIBLE
        })

        binding.spinner.setOnClickListener {
            SmallCanteenListDialogFragment.newInstance(REQUEST_SELECT_CANTEEN).show(supportFragmentManager)
        }

        binding.spinnerImage.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_down_black_24dp)!!).apply {
            DrawableCompat.setTint(this, settings.selectedThemeIconColor(resources.configuration))
        })

        supportFragmentManager.setFragmentResultListener(REQUEST_SELECT_CANTEEN, this) { _, bundle ->
            if (bundle.containsKey(SmallCanteenListDialogFragment.RESULT_CANTEEN_ID)) {
                model.currentlySelectedCanteenId.value = bundle.getInt(SmallCanteenListDialogFragment.RESULT_CANTEEN_ID)
            }
        }

        // do background query of data after canteen selection
        model.currentlySelectedCanteenId.observe(this, Observer {
            lastSnackbar?.dismiss()
        })

        // show sync notifications
        model.syncStatus.observe(this, Observer {
            if (it == MealSyncingDone) {
                lastSnackbar = Snackbar.make(binding.pager, R.string.sync_snackbar_done, Snackbar.LENGTH_SHORT).apply { show() }

                model.confirmSyncStatus()
            } else if (it == MealSyncingFailed) {
                lastSnackbar = Snackbar.make(binding.pager, R.string.sync_snackbar_failed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.sync_snackbar_retry) { model.refresh(true) }
                        .apply { show() }

                model.confirmSyncStatus()
            } else if (it == MealSyncingRunning) {
                lastSnackbar = Snackbar.make(binding.pager, R.string.sync_snackbar_running, Snackbar.LENGTH_SHORT).apply { show() }
            }
        })

        // update the widgets
        if (savedInstanceState == null) MealWidget.updateAppWidgets(this)

        enableEdgeToEdge()

        setContent {
            val status by newModel.screen.collectAsState(ViewerModelInterface.Screen.Initializing)

            val canteenHeader = status.let { when (it) {
                is ViewerModelInterface.Screen.Data -> Pair(
                    it.currentCanteen.database, it.currentCanteen.isFavorite
                )
                else -> null
            } }

            MaterialTheme {
                Scaffold (
                    topBar = {
                        TopAppBar(
                            title = {
                                if (canteenHeader == null) Text(stringResource(R.string.app_name))
                                else {
                                    Row(
                                        Modifier
                                            .fillMaxSize()
                                            .clickable {
                                            // TODO: implement this dialog in compose
                                            SmallCanteenListDialogFragment.newInstance(REQUEST_SELECT_CANTEEN).show(supportFragmentManager)
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(canteenHeader.first.name)

                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = stringResource(R.string.canteen_list_open)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (canteenHeader != null) {
                                    val isFavorite = canteenHeader.second

                                    IconButton(
                                        onClick = {
                                            if (isFavorite) {
                                                newModel.removeFavoriteCanteen(canteenHeader.first.id)
                                            } else {
                                                newModel.addFavoriteCanteen(canteenHeader.first.id)
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
                                            contentDescription = stringResource(R.string.menu_opeb)
                                        )
                                    }
                                )

                                DropdownMenu(
                                    expanded = dropdownMenuExpanded,
                                    onDismissRequest = { dropdownMenuExpanded = false },
                                    content = {
                                        DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.reload))
                                            },
                                            onClick = {
                                                model.refresh(true)
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.canteen_info))
                                            },
                                            onClick = {
                                                // TODO: implement this dialog in compose
                                                CanteenFragment().show(supportFragmentManager)
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(stringResource(R.string.menu_preferences))
                                            },
                                            onClick = {
                                                startActivity(Intent(this@ViewerActivity, SettingsActivity::class.java))
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                ) { insets ->
                    when (status) {
                        ViewerModelInterface.Screen.Initializing -> {
                            Box(Modifier.fillMaxSize().padding(insets).padding(16.dp)) {
                                CircularProgressIndicator(Modifier.align(Alignment.Center))
                            }
                        }
                        ViewerModelInterface.Screen.Privacy -> {
                            Text("TODO: Privacy", modifier = Modifier.padding(insets))
                        }
                        ViewerModelInterface.Screen.WaitingForInitialCanteenSelection -> {
                            Text("TODO: waiting for initial canteen selection", modifier = Modifier.padding(insets))
                        }
                        is ViewerModelInterface.Screen.Data -> {
                            Text("TODO: Data", modifier = Modifier.padding(insets))
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        CanteenSyncing.runBackgroundSync(context = applicationContext)

        if (isFirstResume) {
            isFirstResume = false
        } else {
            model.refresh(force = false)
        }
    }

    override fun onPause() {
        super.onPause()

        model.saveSelectedCanteenId()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        extraCanteenId?.let { outState.putInt(EXTRA_CANTEEN_ID, it) }
        extraDate?.let { outState.putString(EXTRA_DATE, it) }
        extraMealId?.let { outState.putInt(EXTRA_MEAL_ID, it) }
    }
}
