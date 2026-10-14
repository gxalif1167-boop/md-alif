package com.example.data.repository

import com.example.data.dao.HabitDao
import com.example.data.model.AchievementEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.UserPreferenceEntity
import com.example.util.DateUtils
import com.example.util.HabitAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class HabitRepository(private val dao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = dao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletionEntity>> = dao.getAllCompletions()
    val allAchievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
    val preferences: Flow<UserPreferenceEntity?> = dao.getPreferences()

    suspend fun initializeIfEmpty() {
        val existingPrefs = dao.getPreferencesSync()
        if (existingPrefs == null) {
            dao.savePreferences(UserPreferenceEntity(id = 1, isOnboarded = false))
        }

        val existingAchievements = dao.getAllAchievements().first()
        if (existingAchievements.isEmpty()) {
            dao.insertAchievements(HabitAnalytics.INITIAL_ACHIEVEMENTS)
        }
    }

    suspend fun savePreferences(preferences: UserPreferenceEntity) {
        dao.savePreferences(preferences)
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        val id = dao.insertHabit(habit)
        checkAndUnlockAchievements()
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        dao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        dao.deleteCompletionsForHabit(habitId)
        dao.deleteHabit(habitId)
        checkAndUnlockAchievements()
    }

    suspend fun toggleHabitPause(habitId: Long) {
        val habit = dao.getHabitByIdSync(habitId) ?: return
        dao.updateHabit(habit.copy(isPaused = !habit.isPaused))
    }

    suspend fun toggleCompletion(habitId: Long, date: String): Boolean {
        val currentCompletions = dao.getCompletionsForDate(date).first()
        val existing = currentCompletions.find { it.habitId == habitId }

        val isNowCompleted = if (existing != null) {
            dao.deleteCompletion(habitId, date)
            false
        } else {
            dao.insertCompletion(HabitCompletionEntity(habitId = habitId, date = date))
            true
        }

        checkAndUnlockAchievements()
        return isNowCompleted
    }

    suspend fun seedPresetHabits() {
        val today = DateUtils.todayDateString()
        val habits = HabitAnalytics.PRESET_HABITS.map { it.copy(startDate = today) }
        dao.insertHabits(habits)
        checkAndUnlockAchievements()
    }

    /**
     * Checks milestones and updates achievement progress, returns newly unlocked achievements
     */
    suspend fun checkAndUnlockAchievements(): List<AchievementEntity> {
        val habits = dao.getAllHabits().first()
        val completions = dao.getAllCompletions().first()
        val achievements = dao.getAllAchievements().first().associateBy { it.id }.toMutableMap()

        val newlyUnlocked = mutableListOf<AchievementEntity>()
        val today = DateUtils.todayDateString()
        val now = System.currentTimeMillis()

        // 1. First Step: at least 1 completion
        val totalCompletions = completions.size
        achievements["first_step"]?.let { ach ->
            val progress = totalCompletions.coerceAtMost(1)
            val unlocked = progress >= 1
            if (unlocked && !ach.isUnlocked) {
                val updated = ach.copy(isUnlocked = true, unlockedAt = now, currentProgress = 1)
                dao.upsertAchievement(updated)
                newlyUnlocked.add(updated)
            } else if (!ach.isUnlocked && progress != ach.currentProgress) {
                dao.upsertAchievement(ach.copy(currentProgress = progress))
            }
        }

        // 2. Streaks: Calculate max current streak across all habits
        var maxStreakAcrossHabits = 0
        for (h in habits) {
            val compDates = completions.filter { it.habitId == h.id }.map { it.date }.toSet()
            val streak = HabitAnalytics.calculateCurrentStreak(h, compDates, today)
            if (streak > maxStreakAcrossHabits) {
                maxStreakAcrossHabits = streak
            }
        }

        val streakMilestones = listOf(
            "streak_3" to 3,
            "streak_7" to 7,
            "streak_14" to 14,
            "streak_30" to 30
        )

        for ((id, target) in streakMilestones) {
            achievements[id]?.let { ach ->
                val progress = maxStreakAcrossHabits.coerceAtMost(target)
                val unlocked = progress >= target
                if (unlocked && !ach.isUnlocked) {
                    val updated = ach.copy(isUnlocked = true, unlockedAt = now, currentProgress = target)
                    dao.upsertAchievement(updated)
                    newlyUnlocked.add(updated)
                } else if (!ach.isUnlocked && progress != ach.currentProgress) {
                    dao.upsertAchievement(ach.copy(currentProgress = progress))
                }
            }
        }

        // 3. Centurion: 100 completions
        achievements["century_100"]?.let { ach ->
            val progress = totalCompletions.coerceAtMost(100)
            val unlocked = progress >= 100
            if (unlocked && !ach.isUnlocked) {
                val updated = ach.copy(isUnlocked = true, unlockedAt = now, currentProgress = 100)
                dao.upsertAchievement(updated)
                newlyUnlocked.add(updated)
            } else if (!ach.isUnlocked && progress != ach.currentProgress) {
                dao.upsertAchievement(ach.copy(currentProgress = progress))
            }
        }

        // 4. Perfect Day: Check if today all scheduled habits are completed
        val todayScheduled = habits.filter { HabitAnalytics.isHabitScheduledForDate(it, today) }
        val todayCompletedIds = completions.filter { it.date == today }.map { it.habitId }.toSet()
        val isPerfectToday = todayScheduled.isNotEmpty() && todayCompletedIds.containsAll(todayScheduled.map { it.id })
        achievements["perfect_day"]?.let { ach ->
            if (isPerfectToday && !ach.isUnlocked) {
                val updated = ach.copy(isUnlocked = true, unlockedAt = now, currentProgress = 1)
                dao.upsertAchievement(updated)
                newlyUnlocked.add(updated)
            }
        }

        // 5. Habit Architect: 5 active habits
        achievements["habit_architect"]?.let { ach ->
            val count = habits.size.coerceAtMost(5)
            val unlocked = count >= 5
            if (unlocked && !ach.isUnlocked) {
                val updated = ach.copy(isUnlocked = true, unlockedAt = now, currentProgress = 5)
                dao.upsertAchievement(updated)
                newlyUnlocked.add(updated)
            } else if (!ach.isUnlocked && count != ach.currentProgress) {
                dao.upsertAchievement(ach.copy(currentProgress = count))
            }
        }

        return newlyUnlocked
    }

    suspend fun exportToJson(): String {
        val habits = dao.getAllHabits().first()
        val completions = dao.getAllCompletions().first()
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "HabitFlow")
        root.put("exportedAt", System.currentTimeMillis())

        val habitsArray = JSONArray()
        for (h in habits) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("description", h.description)
            obj.put("icon", h.icon)
            obj.put("colorHex", h.colorHex)
            obj.put("category", h.category)
            obj.put("frequencyType", h.frequencyType)
            obj.put("weekdays", h.weekdays)
            obj.put("weeklyTargetDays", h.weeklyTargetDays)
            obj.put("reminderTime", h.reminderTime ?: JSONObject.NULL)
            obj.put("startDate", h.startDate)
            obj.put("isPaused", h.isPaused)
            obj.put("createdAt", h.createdAt)
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        val completionsArray = JSONArray()
        for (c in completions) {
            val obj = JSONObject()
            obj.put("habitId", c.habitId)
            obj.put("date", c.date)
            obj.put("completedAt", c.completedAt)
            completionsArray.put(obj)
        }
        root.put("completions", completionsArray)

        return root.toString(2)
    }

    suspend fun importFromJson(jsonStr: String): Result<Int> {
        return try {
            val root = JSONObject(jsonStr)
            val habitsArray = root.optJSONArray("habits") ?: return Result.failure(Exception("Invalid JSON: 'habits' array missing"))
            val completionsArray = root.optJSONArray("completions") ?: JSONArray()

            val habitIdMap = mutableMapOf<Long, Long>()

            for (i in 0 until habitsArray.length()) {
                val obj = habitsArray.getJSONObject(i)
                val oldId = obj.getLong("id")
                val habit = HabitEntity(
                    id = 0, // generate new autoincrement ID
                    name = obj.getString("name"),
                    description = obj.optString("description", ""),
                    icon = obj.optString("icon", "sparkles"),
                    colorHex = obj.optString("colorHex", "#4F46E5"),
                    category = obj.optString("category", "Health & Fitness"),
                    frequencyType = obj.optString("frequencyType", "DAILY"),
                    weekdays = obj.optString("weekdays", "1,2,3,4,5,6,7"),
                    weeklyTargetDays = obj.optInt("weeklyTargetDays", 7),
                    reminderTime = if (obj.isNull("reminderTime")) null else obj.optString("reminderTime"),
                    startDate = obj.optString("startDate", DateUtils.todayDateString()),
                    isPaused = obj.optBoolean("isPaused", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                val newId = dao.insertHabit(habit)
                habitIdMap[oldId] = newId
            }

            for (i in 0 until completionsArray.length()) {
                val obj = completionsArray.getJSONObject(i)
                val oldHabitId = obj.getLong("habitId")
                val newHabitId = habitIdMap[oldHabitId]
                if (newHabitId != null) {
                    dao.insertCompletion(
                        HabitCompletionEntity(
                            habitId = newHabitId,
                            date = obj.getString("date"),
                            completedAt = obj.optLong("completedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            checkAndUnlockAchievements()
            Result.success(habitsArray.length())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData() {
        dao.deleteAllCompletions()
        dao.deleteAllHabits()
        dao.deleteAllAchievements()
        dao.insertAchievements(HabitAnalytics.INITIAL_ACHIEVEMENTS)
        dao.savePreferences(UserPreferenceEntity(id = 1, isOnboarded = true))
    }
}
