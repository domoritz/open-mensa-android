package de.uni_potsdam.hpi.openmensa.ui.widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.Threads
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.WidgetConfiguration
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.Theme
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.CanteenListModel
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.CanteenListViews
import de.uni_potsdam.hpi.openmensa.worker.WidgetInitialLoadDataWorker
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class MealWidgetConfigureActivity : FragmentActivity() {
    private val canteenListModel by viewModels<CanteenListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = SettingsUtils.with(this)

        if (settings.sourceUrl.isBlank()) {
            Toast.makeText(this, R.string.widget_requires_app_setup_toast, Toast.LENGTH_SHORT).show()

            finish()

            return
        }

        val context = applicationContext
        val database = AppDatabase.with(this)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        val locationAccessResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // ignore
        }

        lifecycleScope.launch {
            canteenListModel.startSelectionIfIdle()

            canteenListModel.activityCommandChannel.consumeEach { command ->
                when (command) {
                    is CanteenListModel.ActivityCommand.RequestLocationAccess -> {
                        locationAccessResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    is CanteenListModel.ActivityCommand.HandleCanteenSelection -> {
                        val canteenId = command.canteenId

                        Threads.database.execute {
                            database.widgetConfiguration.insert(WidgetConfiguration(
                                widgetId = appWidgetId,
                                canteenId = canteenId
                            ))

                            Threads.handler.post {
                                MealWidget.updateAppWidgets(
                                    context,
                                    appWidgetManager,
                                    intArrayOf(appWidgetId)
                                )

                                WidgetInitialLoadDataWorker.enqueue(context, canteenId)
                            }
                        }

                        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

                        finish()
                    }
                    is CanteenListModel.ActivityCommand.HandleCanteenSelectionCancellation -> {
                        finish()
                    }
                }
            }
        }

        setContent {
            Theme {
                val content by canteenListModel.screen.collectAsState()

                CanteenListViews.View(content)
            }
        }
    }
}