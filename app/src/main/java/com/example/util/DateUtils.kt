package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val fullDisplayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun todayDateString(): String {
        return isoFormat.format(Date())
    }

    fun formatDateDisplay(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            displayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatFullDateDisplay(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            fullDisplayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormat.format(cal.time)
    }

    fun getDayOfWeekShort(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            dayOfWeekFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Returns ISO weekday number: Monday = 1, Tuesday = 2, ..., Sunday = 7
     */
    fun getIsoDayOfWeek(dateStr: String): Int {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            when (dayOfWeek) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Returns list of ISO date strings for the past N days (including today) in chronological order
     */
    fun getPastDays(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            list.add(isoFormat.format(c.time))
        }
        return list
    }

    fun addDays(dateStr: String, days: Int): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            isoFormat.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun isFutureDate(dateStr: String): Boolean {
        val today = todayDateString()
        return dateStr > today
    }

    fun isToday(dateStr: String): Boolean {
        return dateStr == todayDateString()
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }
}
