package de.uni_potsdam.hpi.openmensa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.uni_potsdam.hpi.openmensa.data.model.WidgetConfiguration

@Dao
interface WidgetConfigurationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: WidgetConfiguration)

    @Query("SELECT * FROM widget_configuration WHERE widget_id IN (:widgetIds)")
    fun getByWidgetIdsSync(widgetIds: IntArray): List<WidgetConfiguration>

    @Query("SELECT widget_id FROM widget_configuration WHERE canteen_id = :canteenId")
    fun getWidgetIdsByCanteenId(canteenId: Int): IntArray

    @Query("SELECT DISTINCT canteen_id FROM widget_configuration WHERE widget_id IN (:widgetIds)")
    fun getCanteenIdsByWidgetIds(widgetIds: IntArray): List<Int>

    @Query("DELETE FROM widget_configuration WHERE widget_id IN (:widgetIds)")
    fun deleteByWidgetId(widgetIds: IntArray)

    @Query("DELETE FROM widget_configuration")
    fun deleteAll()
}