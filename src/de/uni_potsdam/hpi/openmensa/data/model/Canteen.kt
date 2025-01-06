package de.uni_potsdam.hpi.openmensa.data.model

import android.util.JsonReader
import android.util.JsonToken
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(
            name = "canteen_city_index",
            value = ["city"]
        )
    ]
)
data class Canteen(
        @PrimaryKey
        val id: Int,
        val name: String,
        val city: String,
        val address: String,
        // no coordinates => latitude = longitude = 0
        val latitude: Double,
        val longitude: Double
) {
        companion object {
                fun parse(reader: JsonReader): Canteen {
                        var id: Int? = null
                        var name: String? = null
                        var city: String = ""
                        var address: String? = null
                        var latitude: Double = 0.0
                        var longitude: Double = 0.0

                        reader.beginObject()
                        while (reader.hasNext()) {
                                when (reader.nextName()) {
                                        "id" -> id = reader.nextInt()
                                        "name" -> name = reader.nextString()
                                        "city" -> city = reader.nextString()
                                        "address" -> address =
                                                if (reader.peek() == JsonToken.NULL) { reader.nextNull(); "" }
                                                else reader.nextString()
                                        "coordinates" -> {
                                                if (reader.peek() == JsonToken.NULL) {
                                                        reader.nextNull()
                                                } else {
                                                        reader.beginArray()
                                                        latitude = reader.nextDouble()
                                                        longitude = reader.nextDouble()
                                                        reader.endArray()
                                                }
                                        }
                                        else -> reader.skipValue()
                                }
                        }
                        reader.endObject()

                        return Canteen(
                                id = id!!,
                                name = name!!,
                                city = city,
                                address = address!!,
                                latitude = latitude,
                                longitude = longitude
                        )
                }
        }

        @Transient
        val hasLocation = latitude != 0.0 || longitude != 0.0
}