package com.dktech.baseandroidviewdktech.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dktech.baseandroidviewdktech.data.local.dao.ColoredSegmentDAO
import com.dktech.baseandroidviewdktech.data.local.dao.PaintDAO
import com.dktech.baseandroidviewdktech.data.local.model.ColoredSegment
import com.dktech.baseandroidviewdktech.data.local.model.Paint

@Database(entities = [ColoredSegment::class, Paint::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colorSegmentDAO(): ColoredSegmentDAO

    abstract fun paintDAO(): PaintDAO

    companion object {
        @Suppress("ktlint:standard:property-naming")
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `paint` (`fileName` TEXT NOT NULL, `finishedDate` INTEGER NOT NULL, PRIMARY KEY(`fileName`))")
            }
        }

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "app_database",
                        )
                        .addMigrations(MIGRATION_1_2)
                        .build()
                INSTANCE = instance
                instance
            }
    }
}
