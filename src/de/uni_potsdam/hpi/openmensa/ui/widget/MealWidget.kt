package de.uni_potsdam.hpi.openmensa.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.Threads
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.PendingIntentFlags
import de.uni_potsdam.hpi.openmensa.ui.viewer.ViewerActivity
import de.uni_potsdam.hpi.openmensa.worker.WidgetDataRefreshWorker

class MealWidget : AppWidgetProvider() {
    companion object {
        fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
            appWidgetIds: IntArray = getAppWidgetIds(context, appWidgetManager),
            onCompletion: () -> Unit = {}
        ) {
            if (appWidgetIds.isEmpty()) onCompletion()
            else {
                val database = AppDatabase.with(context)

                val fillInIntent = PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, ViewerActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentFlags.MUTABLE
                )

                Threads.database.execute {
                    val configurations = database.widgetConfiguration.getByWidgetIdsSync(appWidgetIds)
                        .associateBy { it.widgetId }

                    Threads.handler.post {
                        try {
                            for (appWidgetId in appWidgetIds) {
                                val configuration = configurations[appWidgetId]

                                val view =
                                    if (configuration == null) RemoteViews(context.packageName, R.layout.meal_widget_empty).also {
                                        val configIntent = Intent(context, MealWidgetConfigureActivity::class.java)
                                            .setData(Uri.fromParts("config", "$appWidgetId", null))
                                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                                        val configPendingIntent = PendingIntent.getActivity(
                                            context,
                                            1,
                                            configIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentFlags.IMMUTABLE
                                        )

                                        it.setOnClickPendingIntent(R.id.select_canteen_button, configPendingIntent)
                                    }
                                    else RemoteViews(context.packageName, R.layout.meal_widget_list).also {
                                        it.setRemoteAdapter(android.R.id.background, MealWidgetService.intent(context, configuration.canteenId))
                                        it.setPendingIntentTemplate(android.R.id.background, fillInIntent)
                                    }

                                appWidgetManager.updateAppWidget(appWidgetId, view)
                            }

                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list)

                            DayChangeReceiver.schedule(context)
                        } finally {
                            onCompletion()
                        }
                    }
                }
            }
        }

        fun getAppWidgetIds(
            context: Context,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        ): IntArray = appWidgetManager.getAppWidgetIds(ComponentName(context, MealWidget::class.java))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        WidgetDataRefreshWorker.schedule(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()

        updateAppWidgets(context, appWidgetManager, appWidgetIds) { pendingResult.finish() }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val database = AppDatabase.with(context)
        val pendingResult = goAsync()

        Threads.database.execute {
            try {
                database.widgetConfiguration.deleteByWidgetId(appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDisabled(context: Context) {
        val database = AppDatabase.with(context)
        val pendingResult = goAsync()

        WidgetDataRefreshWorker.disable(context)

        Threads.database.execute {
            try {
                database.widgetConfiguration.deleteAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}