package de.uni_potsdam.hpi.openmensa.ui.viewer

import de.uni_potsdam.hpi.openmensa.data.model.Meal

data class CanteenDay(
    val date: String,
    val content: Content
) {
    sealed class Content {
        object Closed: Content()
        object NoInformation: Content()
        data class Data(val meals: List<Meal>): Content()
    }
}