package de.uni_potsdam.hpi.openmensa.extension

import android.location.LocationListener
import android.location.LocationManager
import java.lang.IllegalArgumentException

fun LocationManager.requestLocationUpdatesIfSupported(provider: String, minTime: Long, minDistance: Float, listener: LocationListener): Boolean {
    return try {
        this.requestLocationUpdates(provider, minTime, minDistance, listener)

        true
    } catch (ex: IllegalArgumentException) {
        // not supported location provider -> ignore

        true
    } catch (ex: SecurityException) {
        // no success -> return error

        false
    }
}