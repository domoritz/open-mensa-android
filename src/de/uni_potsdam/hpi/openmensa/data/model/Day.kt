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
)

data class DayWithMeals (
        val date: String,
        val closed: Boolean,
        val meals: List<Meal>
) {
    companion object {
        fun parse(reader: JsonReader, canteenId: Int): DayWithMeals {
            var date: String? = null
            var closed: Boolean? = null
            var meals: List<Meal>? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "date" -> date = reader.nextString()
                    "closed" -> closed = reader.nextBoolean()
                    "meals" -> meals = mutableListOf<Meal>().let { list ->
                        // note: we don't know if date or meals come first, so the meals don't get a date until the end
                        reader.beginArray()
                        while (reader.hasNext()) {
                            list.add(Meal.parse(reader, canteenId, "dummy"))
                        }
                        reader.endArray()

                        list
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            return DayWithMeals(
                    date = date!!,
                    closed = closed!!,
                    meals = meals!!.map { it.copy(date = date) }
            )
        }
    }

    fun toDay(canteenId: Int) = Day(date = date, closed = closed, canteenId = canteenId)
}