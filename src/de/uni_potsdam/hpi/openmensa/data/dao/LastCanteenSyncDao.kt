package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import de.uni_potsdam.hpi.openmensa.data.model.LastCanteenSync

@Dao
interface LastCanteenSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(item: LastCanteenSync)
}