package com.apptracker.sdk.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room database for storing events locally
@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppTrackerDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppTrackerDatabase? = null

        fun getDatabase(context: Context): AppTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppTrackerDatabase::class.java,
                    "apptracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}



