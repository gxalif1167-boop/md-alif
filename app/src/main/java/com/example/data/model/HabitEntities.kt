package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val icon: String = "sparkles",
    val colorHex: String = "#4F46E5",
    val category: String = "Health & Fitness",
    val frequencyType: String = "DAILY", // "DAILY", "WEEKDAYS", "WEEKLY_TARGET"
    val weekdays: String = "1,2,3,4,5,6,7", // comma-separated 1 (Mon) to 7 (Sun)
    val weeklyTargetDays: Int = 7,
    val reminderTime: String? = null, // "HH:mm"
    val startDate: String, // "YYYY-MM-DD"
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_completions",
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val firstDayOfWeek: String = "MONDAY", // "MONDAY", "SUNDAY"
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "20:00",
    val isOnboarded: Boolean = false
)
