package de.uni_potsdam.hpi.openmensa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.uni_potsdam.hpi.openmensa.data.dao.CanteenDao
import de.uni_potsdam.hpi.openmensa.data.dao.DayDao
import de.uni_potsdam.hpi.openmensa.data.dao.MealDao
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.data.model.Meal

@Database(
        entities = [
            Canteen::class,
            Day::class,
            Meal::class
        ], version = 1
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
                        ).build()
                    }
                }
            }

            return instance!!
        }
    }

    abstract fun canteen(): CanteenDao
    abstract fun day(): DayDao
    abstract fun meal(): MealDao
}