package de.uni_potsdam.hpi.openmensa.ui.day

import de.uni_potsdam.hpi.openmensa.data.model.Meal

sealed class MealItem {
    data class Category(val title: String): MealItem()
    data class ShortInfo(val meal: Meal): MealItem()
    data class DetailInfo(val meal: Meal): MealItem()
}