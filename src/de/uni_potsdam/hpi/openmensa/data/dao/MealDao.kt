package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(meals: List<Meal>)

    @Query("DELETE FROM meal WHERE canteen_id = :canteenId AND NOT id IN (:currentItemIds)")
    fun deleteOldItems(canteenId: Int, currentItemIds: List<Int>)

    @Query("SELECT * FROM meal WHERE canteen_id = :canteenId")
    fun getByCanteenSync(canteenId: Int): List<Meal>

    @Query("SELECT * FROM meal WHERE canteen_id = :canteenId")
    fun getByCanteenFlow(canteenId: Int): Flow<List<Meal>>
}