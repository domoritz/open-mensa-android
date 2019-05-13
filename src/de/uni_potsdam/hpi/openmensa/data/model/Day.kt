package de.uni_potsdam.hpi.openmensa.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
        primaryKeys = ["canteen_id", "date"],
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
data class Day (
        @ColumnInfo(name = "canteen_id")
        val canteenId: Int,
        val date: String,
        val closed: Boolean
)