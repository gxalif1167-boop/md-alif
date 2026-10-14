package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.HabitDao
import com.example.data.model.AchievementEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.UserPreferenceEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        AchievementEntity::class,
        UserPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HabitFlowDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitFlowDatabase? = null

        fun getDatabase(context: Context): HabitFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitFlowDatabase::class.java,
                    "habitflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
