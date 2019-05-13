package de.uni_potsdam.hpi.openmensa.ui.canteenlist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import de.uni_potsdam.hpi.openmensa.MainActivity

object LocationUtil {
    fun hasLocationAccessPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun getLastBestLocation(context: Context): LocationStatus {
        if (!hasLocationAccessPermission(context)) {
            return MissingPermissionLocationStatus
        }

        try {
            val locationGPS: Location? = MainActivity.getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val locationNet: Location? = MainActivity.getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

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
}

sealed class LocationStatus
object UnknownLocationStatus: LocationStatus()
object MissingPermissionLocationStatus: LocationStatus()
data class KnownLocationStatus(val location: Location): LocationStatus()