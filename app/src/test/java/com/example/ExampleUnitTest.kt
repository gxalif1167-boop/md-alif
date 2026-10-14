package com.example

import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.util.HabitAnalytics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDailyHabitStreakCalculation() {
        val habit = HabitEntity(
            id = 1,
            name = "Drink Water",
            startDate = "2026-10-10",
            frequencyType = "DAILY"
        )

        val completionDates = setOf(
            "2026-10-12",
            "2026-10-13",
            "2026-10-14"
        )

        val streak = HabitAnalytics.calculateCurrentStreak(
            habit = habit,
            completionDates = completionDates,
            todayStr = "2026-10-14"
        )

        assertEquals(3, streak)
    }

    @Test
    fun testBestStreakCalculation() {
        val habit = HabitEntity(
            id = 1,
            name = "Read",
            startDate = "2026-10-01",
            frequencyType = "DAILY"
        )

        // 4 consecutive days (Oct 1-4), then missed, then 2 days (Oct 7-8)
        val completionDates = setOf(
            "2026-10-01",
            "2026-10-02",
            "2026-10-03",
            "2026-10-04",
            "2026-10-07",
            "2026-10-08"
        )

        val bestStreak = HabitAnalytics.calculateBestStreak(
            habit = habit,
            completionDates = completionDates,
            todayStr = "2026-10-14"
        )

        assertEquals(4, bestStreak)
    }

    @Test
    fun testConsistencyScore() {
        val habits = listOf(
            HabitEntity(
                id = 1,
                name = "Workout",
                startDate = "2026-09-01",
                frequencyType = "DAILY"
            )
        )

        val completions = listOf(
            HabitCompletionEntity(habitId = 1, date = "2026-10-14"),
            HabitCompletionEntity(habitId = 1, date = "2026-10-13")
        )

        val score = HabitAnalytics.calculateConsistencyScore(
            habits = habits,
            allCompletions = completions,
            pastDaysCount = 2
        )

        assertEquals(100, score)
    }

    @Test
    fun testMotivationalMessage() {
        val message100 = HabitAnalytics.getMotivationalMessage(1.0f)
        assertTrue(message100.contains("conquered every single habit"))

        val message0 = HabitAnalytics.getMotivationalMessage(0.0f)
        assertTrue(message0.contains("fresh"))
    }
}
