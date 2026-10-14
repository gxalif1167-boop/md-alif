package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.ui.components.HabitCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Mint500
import com.example.util.DateUtils
import com.example.util.HabitAnalytics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    habits: List<HabitEntity>,
    allCompletions: List<HabitCompletionEntity>,
    selectedDate: String,
    onDateSelect: (String) -> Unit,
    firstDayOfWeek: String, // "MONDAY" or "SUNDAY"
    onToggleCompletion: (Long, String) -> Unit,
    onClickDetail: (HabitEntity) -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onTogglePause: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCal = remember(selectedDate) {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d = sdf.parse(selectedDate)
            if (d != null) cal.time = d
        } catch (_: Exception) {}
        cal
    }

    var displayYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }

    val monthName = remember(displayYear, displayMonth) {
        DateUtils.formatMonthYear(displayYear, displayMonth)
    }

    val todayStr = remember { DateUtils.todayDateString() }

    // Prepare calendar grid days
    val calendarDays = remember(displayYear, displayMonth, firstDayOfWeek) {
        val list = mutableListOf<String?>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, displayYear)
        cal.set(Calendar.MONTH, displayMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon... 7=Sat
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val leadingBlanks = if (firstDayOfWeek == "SUNDAY") {
            firstDayOfMonth - 1
        } else {
            // Monday start: Sun=6, Mon=0, Tue=1...
            if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2
        }

        for (i in 0 until leadingBlanks) {
            list.add(null)
        }

        for (d in 1..daysInMonth) {
            val dateCal = Calendar.getInstance()
            dateCal.set(Calendar.YEAR, displayYear)
            dateCal.set(Calendar.MONTH, displayMonth)
            dateCal.set(Calendar.DAY_OF_MONTH, d)
            val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dateCal.time)
            list.add(iso)
        }

        list
    }

    // Selected date habits
    val scheduledForSelectedDate = remember(habits, selectedDate) {
        habits.filter { HabitAnalytics.isHabitScheduledForDate(it, selectedDate) }
    }

    val selectedDateCompletedIds = remember(allCompletions, selectedDate) {
        allCompletions.filter { it.date == selectedDate }.map { it.habitId }.toSet()
    }

    val selectedDoneCount = scheduledForSelectedDate.count { selectedDateCompletedIds.contains(it.id) }
    val selectedTotalCount = scheduledForSelectedDate.size
    val selectedRate = if (selectedTotalCount > 0) (selectedDoneCount * 100 / selectedTotalCount) else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Navigation Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row {
                    IconButton(
                        onClick = {
                            if (displayMonth == 0) {
                                displayMonth = 11
                                displayYear--
                            } else {
                                displayMonth--
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }

                    IconButton(
                        onClick = {
                            if (displayMonth == 11) {
                                displayMonth = 0
                                displayYear++
                            } else {
                                displayMonth++
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Calendar Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Weekday Labels
                    val weekLabels = if (firstDayOfWeek == "SUNDAY") {
                        listOf("S", "M", "T", "W", "T", "F", "S")
                    } else {
                        listOf("M", "T", "W", "T", "F", "S", "S")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekLabels.forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid rows
                    val rows = calendarDays.chunked(7)
                    rows.forEach { rowDays ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowDays.forEach { dateStr ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dateStr != null) {
                                        val dayNum = dateStr.takeLast(2).toInt().toString()
                                        val isSelected = dateStr == selectedDate
                                        val isToday = dateStr == todayStr
                                        val isFuture = DateUtils.isFutureDate(dateStr)

                                        val scheduled = habits.filter { HabitAnalytics.isHabitScheduledForDate(it, dateStr) }
                                        val completedIds = allCompletions.filter { it.date == dateStr }.map { it.habitId }.toSet()
                                        val isCompletedAll = scheduled.isNotEmpty() && completedIds.containsAll(scheduled.map { it.id })
                                        val isPartial = scheduled.isNotEmpty() && completedIds.isNotEmpty() && !isCompletedAll

                                        val cellColor = when {
                                            isSelected -> Indigo600
                                            isCompletedAll -> Mint500
                                            isPartial -> Amber500.copy(alpha = 0.85f)
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }

                                        val textColor = when {
                                            isSelected || isCompletedAll -> Color.White
                                            isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(cellColor)
                                                .then(
                                                    if (isToday && !isSelected && !isCompletedAll) {
                                                        Modifier.border(1.5.dp, Indigo600, CircleShape)
                                                    } else Modifier
                                                )
                                                .clickable { onDateSelect(dateStr) }
                                                .testTag("cal_date_$dateStr"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected || isToday || isCompletedAll) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 12.sp
                                                ),
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                            // Fill remaining slots in last row if needed
                            if (rowDays.size < 7) {
                                for (i in rowDays.size until 7) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(color = Mint500, label = "Completed")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendDot(color = Amber500, label = "Partial")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendDot(color = Indigo600, label = "Selected")
                    }
                }
            }
        }

        // Inspector Header for Selected Date
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DateUtils.formatFullDateDisplay(selectedDate),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedTotalCount > 0) "$selectedDoneCount of $selectedTotalCount habits done ($selectedRate%)" else "No scheduled habits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedTotalCount > 0 && selectedDoneCount == selectedTotalCount) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Mint500)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "100% Done",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Habit list for this selected date
        if (scheduledForSelectedDate.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits scheduled for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(scheduledForSelectedDate, key = { it.id }) { habit ->
                val isDone = selectedDateCompletedIds.contains(habit.id)
                val compDates = allCompletions.filter { it.habitId == habit.id }.map { it.date }.toSet()
                val streak = HabitAnalytics.calculateCurrentStreak(habit, compDates, selectedDate)

                HabitCard(
                    habit = habit,
                    isCompleted = isDone,
                    streak = streak,
                    onToggleCompletion = { onToggleCompletion(habit.id, selectedDate) },
                    onClickDetail = { onClickDetail(habit) },
                    onEdit = { onEditHabit(habit) },
                    onTogglePause = { onTogglePause(habit.id) },
                    onDelete = { onDeleteHabit(habit.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
