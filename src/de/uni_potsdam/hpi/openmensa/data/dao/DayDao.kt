package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.Day
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(items: List<Day>)

    @Query("DELETE FROM day WHERE canteen_id = :canteenId AND NOT date IN (:currentDates)")
    fun deleteOldItems(canteenId: Int, currentDates: List<String>)

    @Query("SELECT * FROM day WHERE canteen_id = :canteenId")
    fun getByCanteenIdSync(canteenId: Int): List<Day>

    @Query("SELECT * FROM day WHERE canteen_id = :canteenId")
    fun getByCanteenIdFlow(canteenId: Int): Flow<List<Day>>
}