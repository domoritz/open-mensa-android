package de.uni_potsdam.hpi.openmensa.data.model

import androidx.room.*
import de.uni_potsdam.hpi.openmensa.data.converter.StringListJsonConverter

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
        @Embedded
        val prices: Prices,
        val notes: List<String>
)

data class Prices(
        @ColumnInfo(name = "price_student")
        val students: Double?,
        @ColumnInfo(name = "price_employee")
        val employees: Double?,
        @ColumnInfo(name = "price_pupil")
        val pupils: Double?,
        @ColumnInfo(name = "price_other")
        val others: Double
)