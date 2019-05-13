package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.LastCanteenSync

@Dao
interface LastCanteenSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(item: LastCanteenSync)

    @Query("SELECT * FROM last_canteen_sync WHERE canteen_id = :canteenId")
    fun getByCanteenIdSync(canteenId: Int): LastCanteenSync?
}