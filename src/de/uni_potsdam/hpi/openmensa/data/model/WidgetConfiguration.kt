package de.uni_potsdam.hpi.openmensa.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "widget_configuration",
    foreignKeys = [
        ForeignKey(
            entity = Canteen::class,
            parentColumns = ["id"],
            childColumns = ["canteen_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WidgetConfiguration (
    @PrimaryKey
    @ColumnInfo(name = "widget_id")
    val widgetId: Int,
    @ColumnInfo(name = "canteen_id", index = true)
    val canteenId: Int
)