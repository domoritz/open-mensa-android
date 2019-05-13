package de.uni_potsdam.hpi.openmensa.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Canteen(
        @PrimaryKey
        val id: Int,
        val name: String,
        val city: String,
        val address: String,
        // no coordinates => latitude = longitude = 0
        val latitude: Double,
        val longitude: Double
)