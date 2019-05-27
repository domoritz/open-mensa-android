package de.uni_potsdam.hpi.openmensa.ui.settings.canteenlist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import de.uni_potsdam.hpi.openmensa.Threads
import de.uni_potsdam.hpi.openmensa.extension.requestLocationUpdatesIfSupported

object LocationUtil {
    fun hasLocationAccessPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun getLastBestLocation(context: Context): LocationStatus {
        if (!hasLocationAccessPermission(context)) {
            return MissingPermissionLocationStatus
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            val locationGPS: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val locationNet: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val gpsLocationTime = locationGPS?.time ?: 0
            val networkLocationTime = locationNet?.time ?: 0

            val locationToUse = if (networkLocationTime < gpsLocationTime) {
                locationGPS
            } else {
                locationNet
            }

            if (locationToUse != null) {
                return KnownLocationStatus(locationToUse)
            } else {
                return UnknownLocationStatus
            }
        } catch (ex: SecurityException) {
            return MissingPermissionLocationStatus
        }
    }

    fun getLocationLive(context: Context) = object: LiveData<LocationStatus>() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object: LocationListener {
            override fun onLocationChanged(location: Location?) {
                update()
            }

            override fun onProviderDisabled(provider: String?) {
                // ignore
            }

            override fun onProviderEnabled(provider: String?) {
                // ignore
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // ignore
            }
        }

        fun update() {
            val newValue = getLastBestLocation(context)

            if (newValue != value) {
                value = newValue
            }
        }

        lateinit var registerRunnable: Runnable

        init {
            registerRunnable = Runnable {
                val success1 = locationManager.requestLocationUpdatesIfSupported(LocationManager.GPS_PROVIDER, 10 * 1000L, 1000.0f, locationListener)
                val success2 = locationManager.requestLocationUpdatesIfSupported(LocationManager.NETWORK_PROVIDER, 10 * 1000L, 1000.0f, locationListener)
                val success = success1 && success2

                if (!success) {
                    // retry later
                    Threads.handler.postDelayed(registerRunnable, 1000)
                }
            }
        }

        override fun onActive() {
            super.onActive()

            update()
            Threads.handler.post(registerRunnable)
        }

        override fun onInactive() {
            super.onInactive()

            Threads.handler.removeCallbacks(registerRunnable)
            locationManager.removeUpdates(locationListener)
        }
    }
}

sealed class LocationStatus
object UnknownLocationStatus: LocationStatus()
object MissingPermissionLocationStatus: LocationStatus()
data class KnownLocationStatus(val location: Location): LocationStatus()