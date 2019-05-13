package de.uni_potsdam.hpi.openmensa.data.model

import android.util.JsonReader
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
) {
    companion object {
        fun parse(reader: JsonReader, canteenId: Int): Day {
            var date: String? = null
            var closed: Boolean? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "date" -> date = reader.nextString()
                    "closed" -> closed = reader.nextBoolean()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            return Day(
                    canteenId = canteenId,
                    date = date!!,
                    closed = closed!!
            )
        }
    }
}