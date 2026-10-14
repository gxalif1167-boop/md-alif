package com.example.util

import com.example.data.model.AchievementEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity

object HabitAnalytics {

    val PRESET_HABITS = listOf(
        HabitEntity(
            name = "Drink Water",
            description = "Stay hydrated with at least 8 glasses of water",
            icon = "water",
            colorHex = "#0EA5E9",
            category = "Health & Fitness",
            frequencyType = "DAILY",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Read a Book",
            description = "Read 15-20 pages of an inspiring book",
            icon = "book",
            colorHex = "#8B5CF6",
            category = "Learning",
            frequencyType = "DAILY",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Exercise",
            description = "30 minutes workout, cardio, or gym session",
            icon = "exercise",
            colorHex = "#10B981",
            category = "Health & Fitness",
            frequencyType = "WEEKDAYS",
            weekdays = "1,2,3,4,5",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Meditate",
            description = "10 minutes mindful breathing or meditation",
            icon = "meditate",
            colorHex = "#6366F1",
            category = "Mindfulness",
            frequencyType = "DAILY",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Sleep on Time",
            description = "Wind down and sleep before 11:00 PM",
            icon = "sleep",
            colorHex = "#3B82F6",
            category = "Health & Fitness",
            frequencyType = "DAILY",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Study",
            description = "Dedicated deep-work learning session",
            icon = "study",
            colorHex = "#F59E0B",
            category = "Learning",
            frequencyType = "WEEKDAYS",
            weekdays = "1,2,3,4,5",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Journal",
            description = "Reflect on gratitude, wins, and thoughts",
            icon = "journal",
            colorHex = "#EC4899",
            category = "Mindfulness",
            frequencyType = "DAILY",
            startDate = DateUtils.todayDateString()
        ),
        HabitEntity(
            name = "Daily Walk",
            description = "Take a refreshing brisk outdoor walk",
            icon = "walk",
            colorHex = "#14B8A6",
            category = "Health & Fitness",
            frequencyType = "DAILY",
            startDate = DateUtils.todayDateString()
        )
    )

    val INITIAL_ACHIEVEMENTS = listOf(
        AchievementEntity(
            id = "first_step",
            title = "First Step",
            description = "Complete your very first habit",
            icon = "sparkles",
            maxProgress = 1
        ),
        AchievementEntity(
            id = "streak_3",
            title = "3-Day Spark",
            description = "Build momentum with a 3-day streak",
            icon = "flame",
            maxProgress = 3
        ),
        AchievementEntity(
            id = "streak_7",
            title = "Weekly Warrior",
            description = "Maintain a solid 7-day streak",
            icon = "crown",
            maxProgress = 7
        ),
        AchievementEntity(
            id = "streak_14",
            title = "Habit Master",
            description = "Master discipline with a 14-day streak",
            icon = "trophy",
            maxProgress = 14
        ),
        AchievementEntity(
            id = "streak_30",
            title = "Monthly Legend",
            description = "Achieve the ultimate 30-day streak",
            icon = "star",
            maxProgress = 30
        ),
        AchievementEntity(
            id = "century_100",
            title = "Centurion",
            description = "Complete 100 total habit sessions",
            icon = "shield",
            maxProgress = 100
        ),
        AchievementEntity(
            id = "perfect_day",
            title = "Perfect Day",
            description = "Complete 100% of all scheduled habits in a single day",
            icon = "check_circle",
            maxProgress = 1
        ),
        AchievementEntity(
            id = "habit_architect",
            title = "Habit Architect",
            description = "Create 5 or more custom habits",
            icon = "layers",
            maxProgress = 5
        )
    )

    fun isHabitScheduledForDate(habit: HabitEntity, dateStr: String): Boolean {
        if (dateStr < habit.startDate) return false
        if (habit.isPaused) return false

        return when (habit.frequencyType) {
            "DAILY" -> true
            "WEEKDAYS" -> {
                val isoDay = DateUtils.getIsoDayOfWeek(dateStr).toString()
                val activeDays = habit.weekdays.split(",").map { it.trim() }
                activeDays.contains(isoDay)
            }
            "WEEKLY_TARGET" -> true
            else -> true
        }
    }

    /**
     * Calculates the current active streak for a single habit.
     * Skips non-scheduled days so streaks are not punitively broken on off-days.
     */
    fun calculateCurrentStreak(
        habit: HabitEntity,
        completionDates: Set<String>,
        todayStr: String = DateUtils.todayDateString()
    ): Int {
        if (habit.isPaused) return 0

        var streak = 0
        var checkDate = todayStr
        val isTodayScheduled = isHabitScheduledForDate(habit, checkDate)

        // If today is scheduled and completed, start with today
        if (isTodayScheduled && completionDates.contains(checkDate)) {
            streak++
            checkDate = DateUtils.addDays(checkDate, -1)
        } else if (isTodayScheduled && !completionDates.contains(checkDate)) {
            // Today is scheduled but not completed yet; start checking from yesterday so streak isn't lost before day ends
            checkDate = DateUtils.addDays(checkDate, -1)
        } else {
            // Today is not scheduled, start from yesterday
            checkDate = DateUtils.addDays(checkDate, -1)
        }

        // Loop backwards in time
        var safetyCounter = 0
        while (checkDate >= habit.startDate && safetyCounter < 365) {
            safetyCounter++
            if (isHabitScheduledForDate(habit, checkDate)) {
                if (completionDates.contains(checkDate)) {
                    streak++
                    checkDate = DateUtils.addDays(checkDate, -1)
                } else {
                    // Missed a scheduled day; streak ends
                    break
                }
            } else {
                // Not scheduled (e.g. rest day); keep checking backwards without penalizing
                checkDate = DateUtils.addDays(checkDate, -1)
            }
        }
        return streak
    }

    /**
     * Calculates the best historical streak for a habit.
     */
    fun calculateBestStreak(
        habit: HabitEntity,
        completionDates: Set<String>,
        todayStr: String = DateUtils.todayDateString()
    ): Int {
        if (completionDates.isEmpty()) return 0

        var maxStreak = 0
        var currentRunningStreak = 0

        // Traverse from startDate up to today
        var currentDate = habit.startDate
        var safetyCounter = 0
        while (currentDate <= todayStr && safetyCounter < 730) {
            safetyCounter++
            if (isHabitScheduledForDate(habit, currentDate)) {
                if (completionDates.contains(currentDate)) {
                    currentRunningStreak++
                    if (currentRunningStreak > maxStreak) {
                        maxStreak = currentRunningStreak
                    }
                } else {
                    currentRunningStreak = 0
                }
            }
            currentDate = DateUtils.addDays(currentDate, 1)
        }
        return maxStreak
    }

    /**
     * Overall active streak across the entire app
     */
    fun calculateOverallStreak(
        habits: List<HabitEntity>,
        allCompletions: List<HabitCompletionEntity>,
        todayStr: String = DateUtils.todayDateString()
    ): Int {
        if (habits.isEmpty()) return 0
        var streak = 0
        var checkDate = todayStr

        val completionsByDate = allCompletions.groupBy { it.date }

        fun isDaySuccessful(date: String): Boolean {
            val scheduled = habits.filter { isHabitScheduledForDate(it, date) }
            if (scheduled.isEmpty()) return true
            val completedCount = completionsByDate[date]?.map { it.habitId }?.toSet()?.size ?: 0
            return completedCount >= scheduled.size
        }

        // Check today
        if (isDaySuccessful(checkDate)) {
            streak++
            checkDate = DateUtils.addDays(checkDate, -1)
        } else {
            // Check yesterday
            checkDate = DateUtils.addDays(checkDate, -1)
        }

        var safety = 0
        while (safety < 365) {
            safety++
            val scheduled = habits.filter { isHabitScheduledForDate(it, checkDate) }
            if (scheduled.isEmpty()) {
                checkDate = DateUtils.addDays(checkDate, -1)
                continue
            }
            if (isDaySuccessful(checkDate)) {
                streak++
                checkDate = DateUtils.addDays(checkDate, -1)
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Calculates consistency score (0 - 100%) based on the last 30 days
     */
    fun calculateConsistencyScore(
        habits: List<HabitEntity>,
        allCompletions: List<HabitCompletionEntity>,
        pastDaysCount: Int = 30
    ): Int {
        val activeHabits = habits.filter { !it.isPaused }
        if (activeHabits.isEmpty()) return 0

        val pastDays = DateUtils.getPastDays(pastDaysCount)
        val completedSet = allCompletions.map { "${it.habitId}_${it.date}" }.toSet()

        var totalScheduledSlots = 0
        var completedScheduledSlots = 0

        for (day in pastDays) {
            for (habit in activeHabits) {
                if (isHabitScheduledForDate(habit, day)) {
                    totalScheduledSlots++
                    if (completedSet.contains("${habit.id}_$day")) {
                        completedScheduledSlots++
                    }
                }
            }
        }

        if (totalScheduledSlots == 0) return 0
        return ((completedScheduledSlots.toFloat() / totalScheduledSlots) * 100).toInt()
    }

    fun getMotivationalMessage(completionRate: Float): String {
        return when {
            completionRate >= 1.0f -> "Sensational! You conquered every single habit today!"
            completionRate >= 0.75f -> "Outstanding momentum! You're almost at a perfect score!"
            completionRate >= 0.50f -> "Halfway there! Consistency builds champions."
            completionRate > 0.0f -> "Great start! Keep the positive energy flowing."
            else -> "A fresh day, fresh opportunities! Choose one habit to begin."
        }
    }
}
