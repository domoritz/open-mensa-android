package de.uni_potsdam.hpi.openmensa.ui.day

import de.uni_potsdam.hpi.openmensa.data.model.Meal

sealed class MealItem
data class DateMealItem(val date: String)
data class MealCategoryItem(val title: String)
data class MealShortInfoItem(val meal: Meal)
data class MealDetailInfoItem(val meal: Meal)