package de.uni_potsdam.hpi.openmensa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.uni_potsdam.hpi.openmensa.data.dao.*
import de.uni_potsdam.hpi.openmensa.data.model.*

@Database(
        entities = [
            Canteen::class,
            CurrentCanteen::class,
            Day::class,
            Meal::class,
            LastCanteenSync::class
        ], version = 3
)
abstract class AppDatabase: RoomDatabase() {
    companion object {
        private const val DATABASE_NAME = "data"
        private val lock = Object()
        private var instance: AppDatabase? = null

        fun with(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(lock) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                                context,
                                AppDatabase::class.java,
                                DATABASE_NAME
                        ).addMigrations(*Migrations.ALL).build()
                    }
                }
            }

            return instance!!
        }
    }

    abstract fun canteen(): CanteenDao
    abstract fun canteenCity(): CanteenCityDao
    abstract fun currentCanteen(): CurrentCanteenDao
    abstract fun day(): DayDao
    abstract fun meal(): MealDao
    abstract fun lastCanteenSync(): LastCanteenSyncDao
}