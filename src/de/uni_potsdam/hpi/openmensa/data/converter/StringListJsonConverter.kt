package de.uni_potsdam.hpi.openmensa.data.converter

import android.util.JsonReader
import android.util.JsonWriter
import androidx.room.TypeConverter
import java.io.StringReader
import java.io.StringWriter

class StringListJsonConverter {
    @TypeConverter
    fun parse(value: String): List<String> {
        val result = mutableListOf<String>()

        JsonReader(StringReader(value)).let { reader ->
            reader.beginArray()
            while (reader.hasNext()) {
                result.add(reader.nextString())
            }
            reader.endArray()
        }

        return result
    }

    @TypeConverter
    fun serialize(value: List<String>): String = StringWriter().let { writer ->
        JsonWriter(writer).use { jsonWriter ->
            jsonWriter.beginArray()
            value.forEach { jsonWriter.value(it) }
            jsonWriter.endArray()
        }

        writer.buffer.toString()
    }
}