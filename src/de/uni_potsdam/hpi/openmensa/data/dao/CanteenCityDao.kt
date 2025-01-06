package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.CanteenCity
import kotlinx.coroutines.flow.Flow

@Dao
interface CanteenCityDao {
    @Query("SELECT DISTINCT city FROM canteen ORDER BY city ASC")
    fun getCitiesFlow(): Flow<List<CanteenCity>>
}