package de.uni_potsdam.hpi.openmensa.helpers

import android.util.Log
import androidx.lifecycle.LiveData
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.Threads
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val LOG_TAG = "DateUtils"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun loadDateIntoCalendar(date: String, calendar: Calendar) {
        dateFormat.timeZone = calendar.timeZone
        calendar.timeInMillis = dateFormat.parse(date).time
    }

    fun parseToLocalTimezone(date: String): Long {
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.parse(date).time
    }

    fun formatWithLocalTimezone(timestamp: Long): String {
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(Date(timestamp))
    }

    fun format(calendar: Calendar): String {
        dateFormat.timeZone = calendar.timeZone
        return dateFormat.format(Date(calendar.timeInMillis))
    }

    val localDate = object: LiveData<String>() {
        lateinit var updateJob: Runnable

        init {
            updateJob = Runnable {
                Threads.handler.postDelayed(updateJob, 1000)

                update()
            }
        }

        fun update() {
            val now = formatWithLocalTimezone(System.currentTimeMillis())

            if (now != value) {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "update date: $now")
                }

                value = now
            }
        }

        override fun onActive() {
            super.onActive()

            update()
            Threads.handler.post(updateJob)
        }

        override fun onInactive() {
            super.onInactive()

            Threads.handler.removeCallbacks(updateJob)
        }
    }
}