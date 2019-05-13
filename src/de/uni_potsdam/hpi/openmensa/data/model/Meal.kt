package de.uni_potsdam.hpi.openmensa.data.model

import android.util.JsonReader
import androidx.room.*
import de.uni_potsdam.hpi.openmensa.data.converter.StringListJsonConverter
import de.uni_potsdam.hpi.openmensa.data.model.Prices.Companion.isValueEmpty
import de.uni_potsdam.hpi.openmensa.extension.nextNullableDouble

@Entity(
        foreignKeys = [
                ForeignKey(
                        entity = Day::class,
                        parentColumns = ["canteen_id", "date"],
                        childColumns = ["canteen_id", "date"],
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        ],
        indices = [
                Index(name = "meal_canteen_id_date_index", value = ["canteen_id", "date"])
        ]
)
@TypeConverters(StringListJsonConverter::class)
data class Meal(
        @PrimaryKey
        val id: Int,
        @ColumnInfo(name = "canteen_id")
        val canteenId: Int,
        val date: String,
        val name: String,
        val category: String,
        // room does not create object if all values are null -> make it optional
        @Embedded
        val prices: Prices?,
        val notes: List<String>
) {
        companion object {
                fun parse(reader: JsonReader, canteenId: Int, date: String): Meal {
                        var id: Int? = null
                        var name: String? = null
                        var category: String? = null
                        var prices: Prices? = null
                        var notes: List<String>? = null

                        reader.beginObject()
                        while (reader.hasNext()) {
                                when (reader.nextName()) {
                                        "id" -> id = reader.nextInt()
                                        "name" -> name = reader.nextString()
                                        "category" -> category = reader.nextString()
                                        "prices" -> prices = Prices.parse(reader)
                                        "notes" -> notes = mutableListOf<String>().let { list ->
                                                reader.beginArray()
                                                while (reader.hasNext()) {
                                                        list.add(reader.nextString())
                                                }
                                                reader.endArray()

                                                list
                                        }
                                        else -> reader.skipValue()
                                }
                        }
                        reader.endObject()

                        return Meal(
                                id = id!!,
                                canteenId = canteenId,
                                date = date,
                                name = name!!,
                                category = category!!,
                                prices = prices!!,
                                notes = notes!!
                        )
                }
        }
}

data class Prices(
        @ColumnInfo(name = "price_student")
        val students: Double?,
        @ColumnInfo(name = "price_employee")
        val employees: Double?,
        @ColumnInfo(name = "price_pupil")
        val pupils: Double?,
        @ColumnInfo(name = "price_other")
        val others: Double?
) {
        companion object {
                fun parse(reader: JsonReader): Prices {
                        var students: Double? = null
                        var employees: Double? = null
                        var pupils: Double? = null
                        var others: Double? = null

                        reader.beginObject()
                        while (reader.hasNext()) {
                                when (reader.nextName()) {
                                        "students" -> students = reader.nextNullableDouble()
                                        "employees" -> employees = reader.nextNullableDouble()
                                        "pupils" -> pupils = reader.nextNullableDouble()
                                        "others" -> others = reader.nextNullableDouble()
                                        else -> reader.skipValue()
                                }
                        }
                        reader.endObject()

                        return Prices(
                                students = students,
                                employees = employees,
                                pupils = pupils,
                                others = others
                        )
                }

                fun isValueEmpty(item: Double?) = (item ?: 0.0) <= 0.0
        }
}

fun Prices?.isEmpty(): Boolean = (this == null) || (
        isValueEmpty(this.students) &&
                isValueEmpty(this.employees) &&
                isValueEmpty(this.pupils) &&
                isValueEmpty(this.others)
        )