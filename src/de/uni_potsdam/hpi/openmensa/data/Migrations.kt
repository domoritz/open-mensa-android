package de.uni_potsdam.hpi.openmensa.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    private val MIGRATE_TO_V2 = object: Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `current_canteen` (`id` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`id`) REFERENCES `Canteen`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        }
    }

    val ALL = arrayOf<Migration>(
        MIGRATE_TO_V2
    )
}