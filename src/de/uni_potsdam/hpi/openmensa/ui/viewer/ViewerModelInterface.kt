package de.uni_potsdam.hpi.openmensa.ui.viewer

import androidx.compose.material3.SnackbarHostState
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import kotlinx.coroutines.flow.StateFlow

interface ViewerModelInterface {
    val screen: StateFlow<Screen>
    val snackbar: SnackbarHostState

    fun acceptPrivacy(serverUrl: String)
    fun selectCanteen(canteenId: Int)
    fun addFavoriteCanteen(canteenId: Int)
    fun removeFavoriteCanteen(canteenId: Int)
    fun refresh()

    sealed class Screen {
        object Initializing: Screen()

        data class Privacy(val serverUrl: String): Screen()

        object LoadingCanteenListScreen: Screen()

        object NoCanteensKnownScreen: Screen()

        object WaitingForInitialCanteenSelection: Screen()

        data class Data(
            val currentCanteen: CanteenInfo,
            val days: List<CanteenDay>
        ): Screen() {
            data class CanteenInfo(
                val database: Canteen,
                val isFavorite: Boolean
            )
        }
    }

    data class CanteenDay(
        val rawDate: String,
        val absoluteDate: String,
        val relativeDate: String,
        val content: Content
    ) {
        sealed class Content {
            object Closed: Content()
            object NoInformation: Content()
            data class Data(val meals: List<ListItem>): Content()
        }
    }

    sealed class ListItem {
        data class CategoryHeader(val label: String): ListItem()
        data class MealItem(val meal: Meal): ListItem()
    }
}