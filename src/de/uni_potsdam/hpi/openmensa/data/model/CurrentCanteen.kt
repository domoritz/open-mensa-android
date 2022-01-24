package de.uni_potsdam.hpi.openmensa.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "current_canteen",
    foreignKeys = [
        ForeignKey(
            entity = Canteen::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CurrentCanteen (
    @PrimaryKey
    val id: Int
)