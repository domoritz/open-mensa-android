package de.uni_potsdam.hpi.openmensa.ui.viewer

import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import kotlinx.coroutines.flow.Flow

interface ViewerModelInterface {
    val screen: Flow<Screen>

    fun addFavoriteCanteen(canteenId: Int)
    fun removeFavoriteCanteen(canteenId: Int)

    sealed class Screen {
        object Initializing: Screen()

        object Privacy: Screen()

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
}