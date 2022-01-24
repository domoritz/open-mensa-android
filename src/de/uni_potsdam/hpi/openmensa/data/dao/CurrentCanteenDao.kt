package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.CurrentCanteen

@Dao
interface CurrentCanteenDao {
    @Insert()
    fun insert(items: List<CurrentCanteen>)

    @Query("DELETE FROM canteen")
    fun deleteAllItems()
}