package com.example.echorooms.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.echorooms.data.database.dao.MemoryEntryDao
import com.example.echorooms.data.database.dao.RoomDao
import com.example.echorooms.data.database.entity.MemoryEntry
import com.example.echorooms.data.database.entity.RoomEntity

/**
 * EchoRooms Room Database.
 * Stores memory rooms and their associated entries (notes, voice, images, milestones).
 */
@Database(
    entities = [RoomEntity::class, MemoryEntry::class],
    version = 2,
    exportSchema = false
)
abstract class EchoRoomsDatabase : RoomDatabase() {

    abstract fun roomDao(): RoomDao
    abstract fun memoryEntryDao(): MemoryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: EchoRoomsDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE rooms ADD COLUMN customThemeJson TEXT")
                db.execSQL("ALTER TABLE rooms ADD COLUMN isBiometricProtected INTEGER NOT NULL DEFAULT 0")
                
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN weather TEXT")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN locationName TEXT")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN isDeletable INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN sketchPath TEXT")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN valPeace REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN valWarmth REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN valAnxiety REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE memory_entries ADD COLUMN valMelancholy REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): EchoRoomsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EchoRoomsDatabase::class.java,
                    "echorooms_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
