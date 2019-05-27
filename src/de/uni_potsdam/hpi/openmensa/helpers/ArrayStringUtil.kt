package de.uni_potsdam.hpi.openmensa.helpers

import android.util.JsonReader
import android.util.JsonWriter
import java.io.StringReader
import java.io.StringWriter

object ArrayStringUtil {
    fun parse(input: String?): List<String> {
        return if (input.isNullOrEmpty()) {
            emptyList()
        } else {
            val result = mutableListOf<String>()

            JsonReader(StringReader(input)).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    result.add(reader.nextString())
                }
                reader.endArray()
            }

            result
        }
    }

    fun serialize(input: List<String>) = StringWriter().use { stringWriter ->
        JsonWriter(stringWriter).use { writer ->
            writer.beginArray()
            input.forEach { writer.value(it) }
            writer.endArray()
        }

        stringWriter
    }.buffer.toString()
}