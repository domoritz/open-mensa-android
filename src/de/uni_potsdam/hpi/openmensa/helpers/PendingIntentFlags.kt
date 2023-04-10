package de.uni_potsdam.hpi.openmensa.helpers

import android.app.PendingIntent
import android.os.Build

object PendingIntentFlags {
    val IMMUTABLE =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
        else 0

    val MUTABLE =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE
        else 0
}