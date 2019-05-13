package de.uni_potsdam.hpi.openmensa.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
        tableName = "last_canteen_sync",
        foreignKeys = [
            ForeignKey(
                    entity = Canteen::class,
                    parentColumns = ["id"],
                    childColumns = ["canteen_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE
            )
        ]
)
data class LastCanteenSync(
        @PrimaryKey
        @ColumnInfo(name = "canteen_id")
        val canteenId: Int,
        val timestamp: Long
)