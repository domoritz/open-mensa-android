package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.CanteenCity

@Dao
interface CanteenCityDao {
    @Query("SELECT DISTINCT city FROM canteen ORDER BY city ASC")
    fun getCities(): LiveData<List<CanteenCity>>
}