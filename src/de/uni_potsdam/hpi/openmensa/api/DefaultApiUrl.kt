package de.uni_potsdam.hpi.openmensa.api

import android.os.Build

object DefaultApiUrl {
    const val UNSAFE_URL = "http://openmensa.org/api/v2/"
    const val SAFE_URL = "https://openmensa.org/api/v2/"

    val NEEDS_UNSAFE_URL = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
}