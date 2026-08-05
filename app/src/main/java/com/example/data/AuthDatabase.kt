package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, InterviewEntity::class, DailyGoalEntity::class, ResumeScanHistoryEntity::class], version = 5, exportSchema = false)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun interviewDao(): InterviewDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun resumeScanHistoryDao(): ResumeScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AuthDatabase? = null

        fun getDatabase(context: Context): AuthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    "auth_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
