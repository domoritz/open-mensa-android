package de.uni_potsdam.hpi.openmensa

import de.uni_potsdam.hpi.openmensa.worker.WidgetDataRefreshWorker

class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()

        WidgetDataRefreshWorker.update(this)
    }
}