package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.*
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import kotlinx.coroutines.flow.Flow

@Dao
interface CanteenDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(items: List<Canteen>)

    @Update
    fun update(items: List<Canteen>)

    @Query("DELETE FROM canteen WHERE NOT id IN (SELECT id FROM current_canteen)")
    fun deleteOldItems()

    @Query("DELETE FROM canteen")
    fun deleteAllItems()

    @Query("SELECT * FROM canteen")
    fun getAllFlow(): Flow<List<Canteen>>

    @Query("SELECT * FROM canteen WHERE city = :city ORDER BY name")
    fun getByCityFlow(city: String): Flow<List<Canteen>>

    @Query("SELECT * FROM canteen WHERE id = :id")
    fun getByIdSync(id: Int): Canteen?

    @Query("SELECT * FROM canteen WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Canteen?>

    @Query("SELECT COUNT(1) FROM canteen")
    fun countItems(): Flow<Long>
}