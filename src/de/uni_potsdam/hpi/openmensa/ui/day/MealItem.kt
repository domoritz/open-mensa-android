package de.uni_potsdam.hpi.openmensa.ui.day

import de.uni_potsdam.hpi.openmensa.data.model.Meal

sealed class MealItem
data class DateMealItem(val date: String): MealItem()
data class MealCategoryItem(val title: String): MealItem()
data class MealShortInfoItem(val meal: Meal): MealItem()
data class MealDetailInfoItem(val meal: Meal): MealItem()