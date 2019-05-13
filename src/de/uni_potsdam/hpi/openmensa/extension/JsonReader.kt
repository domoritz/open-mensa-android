package de.uni_potsdam.hpi.openmensa.extension

import android.util.JsonReader
import android.util.JsonToken

fun JsonReader.nextNullableDouble(): Double? = if (this.peek() == JsonToken.NULL) {
    this.nextNull(); null
} else
    this.nextDouble()