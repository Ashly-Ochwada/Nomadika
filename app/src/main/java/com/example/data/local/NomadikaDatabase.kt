package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedDestinationEntity::class,
        ItineraryEntity::class,
        GroupTripEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NomadikaDatabase : RoomDatabase() {
    abstract fun nomadikaDao(): NomadikaDao

    companion object {
        @Volatile
        private var INSTANCE: NomadikaDatabase? = null

        fun getDatabase(context: Context): NomadikaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NomadikaDatabase::class.java,
                    "nomadika_travel_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
