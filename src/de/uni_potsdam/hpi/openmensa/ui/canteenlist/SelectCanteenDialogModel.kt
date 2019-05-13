package de.uni_potsdam.hpi.openmensa.ui.canteenlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SelectCanteenDialogModel(application: Application): AndroidViewModel(application) {
    val locationLive = LocationUtil.getLocationLive(application)
}