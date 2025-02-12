package com.onurds.doorbell

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NotificationLogEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class NotificationDatabase : RoomDatabase() {

    // Defining the available database operations
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {

        @Volatile
        private var INSTANCE: NotificationDatabase? = null

        fun getDatabase(context: Context): NotificationDatabase {
            // If the INSTANCE is not null, return it. If it is, create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDatabase::class.java,
                    "notification_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
