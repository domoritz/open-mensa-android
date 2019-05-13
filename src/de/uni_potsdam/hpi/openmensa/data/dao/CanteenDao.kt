package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.Canteen

@Dao
interface CanteenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(items: List<Canteen>)

    @Query("DELETE FROM canteen WHERE NOT id IN (:currentItemIds)")
    fun deleteOldItems(currentItemIds: List<Int>)

    @Query("SELECT * FROM canteen")
    fun getAll(): LiveData<List<Canteen>>
}