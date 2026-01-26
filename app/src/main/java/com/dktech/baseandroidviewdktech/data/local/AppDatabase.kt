package com.dktech.baseandroidviewdktech.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dktech.baseandroidviewdktech.data.local.dao.ColoredSegmentDAO
import com.dktech.baseandroidviewdktech.data.local.model.ColoredSegment

@Database(entities = [ColoredSegment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colorSegmentDAO(): ColoredSegmentDAO

    companion object {
        @Suppress("ktlint:standard:property-naming")
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "app_database",
                        ).build()
                INSTANCE = instance
                instance
            }
    }
}
