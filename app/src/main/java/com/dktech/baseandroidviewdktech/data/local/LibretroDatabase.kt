package com.emulator.retro.console.game.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emulator.retro.console.game.data.local.dao.GameDao
import com.emulator.retro.console.game.data.local.models.LibretroRom

@Database(
    entities = [LibretroRom::class],
    version = 1,
    exportSchema = false,
)
abstract class LibretroDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: LibretroDatabase? = null

        fun getDatabase(context: Context): LibretroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context, LibretroDatabase::class.java, "libretro-db")
                        .createFromAsset("libretro-db.sqlite")
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance

                instance
            }
        }
    }

}
