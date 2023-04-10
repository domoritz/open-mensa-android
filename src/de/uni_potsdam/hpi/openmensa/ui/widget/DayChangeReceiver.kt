package de.uni_potsdam.hpi.openmensa.ui.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.getSystemService
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.helpers.PendingIntentFlags
import java.util.Calendar

class DayChangeReceiver: BroadcastReceiver() {
    companion object {
        private const val LOG_TAG = "DayChangeReceiver"

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService<AlarmManager>()!!

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                Intent(context, DayChangeReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentFlags.IMMUTABLE
            )

            val now = System.currentTimeMillis()

            val tomorrowMillis = Calendar.getInstance().let { calendar ->
                val today = DateUtils.formatWithLocalTimezone(now)

                DateUtils.loadDateIntoCalendar(today, calendar)
                calendar.add(Calendar.DATE, 1)

                calendar.timeInMillis
            }

            alarmManager.set(AlarmManager.RTC, tomorrowMillis, pendingIntent)

            if (BuildConfig.DEBUG) {
                val diff = tomorrowMillis - now

                Log.d(LOG_TAG, "schedule in ${diff/1000} seconds")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onReceive()")
        }

        val pendingResult = goAsync()

        MealWidget.updateAppWidgets(context, onCompletion = { pendingResult.finish() })
    }
}