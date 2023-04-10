package de.uni_potsdam.hpi.openmensa.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.Threads
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.data.model.WidgetConfiguration
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.canteenlist.small.SmallCanteenListDialogFragment
import de.uni_potsdam.hpi.openmensa.worker.WidgetInitialLoadDataWorker

class MealWidgetConfigureActivity : FragmentActivity() {
    companion object {
        private const val PICK_CANTEEN_REQUEST_KEY = "pick canteen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val settings = SettingsUtils.with(this)
        val initialTheme = settings.selectedTranslucentTheme

        setTheme(initialTheme)
        super.onCreate(savedInstanceState)

        settings.selectedTranslucentThemeLive.observe(this) {
            if (it != initialTheme) recreate()
        }

        if (settings.sourceUrl.isBlank()) {
            Toast.makeText(this, R.string.widget_requires_app_setup_toast, Toast.LENGTH_SHORT).show()

            finish()

            return
        }

        val context = applicationContext
        val database = AppDatabase.with(this)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        supportFragmentManager.setFragmentResultListener(PICK_CANTEEN_REQUEST_KEY, this) { _, bundle ->
            if (bundle.containsKey(SmallCanteenListDialogFragment.RESULT_CANTEEN_ID)) {
                val canteenId = bundle.getInt(SmallCanteenListDialogFragment.RESULT_CANTEEN_ID)

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
            }

            finish()
        }

        if (savedInstanceState == null)
            SmallCanteenListDialogFragment.newInstance(PICK_CANTEEN_REQUEST_KEY).show(supportFragmentManager)
    }
}