package de.uni_potsdam.hpi.openmensa.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ClockAdjustedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isValidAction = when (intent.action) {
            Intent.ACTION_TIME_CHANGED -> true
            Intent.ACTION_TIMEZONE_CHANGED -> true
            else -> false
        }

        if (isValidAction) {
            val pendingResult = goAsync()

            MealWidget.updateAppWidgets(context, onCompletion = { pendingResult.finish() })
        }
    }
}