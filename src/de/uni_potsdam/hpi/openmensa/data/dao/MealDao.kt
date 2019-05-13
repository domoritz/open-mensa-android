package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.data.model.Meal

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(meals: List<Meal>)

    @Query("DELETE FROM meal WHERE canteen_id = :canteenId AND NOT id IN (:currentItemIds)")
    fun deleteOldItems(canteenId: Int, currentItemIds: List<Int>)

    @Query("SELECT * FROM meal WHERE canteen_id = :canteenId AND date = :date")
    fun getByCanteenAndDate(canteenId: Int, date: String): LiveData<List<Meal>>
}