package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.ui.components.DailyProgressRing
import com.example.ui.components.HabitCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Mint500
import com.example.util.DateUtils
import com.example.util.HabitAnalytics

@Composable
fun DashboardScreen(
    habits: List<HabitEntity>,
    allCompletions: List<HabitCompletionEntity>,
    onToggleCompletion: (Long) -> Unit,
    onClickDetail: (HabitEntity) -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onTogglePause: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onAddHabit: () -> Unit,
    onSeedPresets: () -> Unit,
    onNavigateCalendar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { DateUtils.todayDateString() }
    val greeting = remember { DateUtils.getGreeting() }
    val todayFormatted = remember { DateUtils.formatDateDisplay(today) }

    // Habits scheduled for today
    val scheduledToday = remember(habits, today) {
        habits.filter { HabitAnalytics.isHabitScheduledForDate(it, today) }
    }

    val todayCompletedIds = remember(allCompletions, today) {
        allCompletions.filter { it.date == today }.map { it.habitId }.toSet()
    }

    val completedCount = scheduledToday.count { todayCompletedIds.contains(it.id) }
    val totalCount = scheduledToday.size
    val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val overallStreak = remember(habits, allCompletions, today) {
        HabitAnalytics.calculateOverallStreak(habits, allCompletions, today)
    }

    val consistencyScore = remember(habits, allCompletions) {
        HabitAnalytics.calculateConsistencyScore(habits, allCompletions, 30)
    }

    val motivationalText = remember(completionRate) {
        HabitAnalytics.getMotivationalMessage(completionRate)
    }

    val past7Days = remember { DateUtils.getPastDays(7) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("dashboard_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Greeting & Date
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Streak Pill
                    if (overallStreak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Amber500.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Current Streak",
                                tint = Amber500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$overallStreak d streak",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Amber500
                            )
                        }
                    }
                }
            }

            // Hero Daily Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Daily Progress",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedCount of $totalCount habits completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Consistency Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Mint500.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$consistencyScore% Consistency",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Mint500
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Circular Progress Ring
                        DailyProgressRing(
                            completedCount = completedCount,
                            totalCount = totalCount,
                            size = 150.dp,
                            strokeWidth = 14.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Motivational Quote Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = motivationalText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Weekly Completion Mini-Row (7 Days)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Flow",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View Calendar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.clickable { onNavigateCalendar(today) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            past7Days.forEach { dayStr ->
                                val dayScheduled = habits.filter { HabitAnalytics.isHabitScheduledForDate(it, dayStr) }
                                val dayCompletions = allCompletions.filter { it.date == dayStr }.map { it.habitId }.toSet()
                                val isAllDone = dayScheduled.isNotEmpty() && dayCompletions.containsAll(dayScheduled.map { it.id })
                                val isPartial = dayScheduled.isNotEmpty() && dayCompletions.isNotEmpty() && !isAllDone
                                val isCurrent = dayStr == today

                                val dayLabel = DateUtils.getDayOfWeekShort(dayStr).take(2)
                                val dayNum = dayStr.takeLast(2)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { onNavigateCalendar(dayStr) }
                                ) {
                                    Text(
                                        text = dayLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isAllDone -> Mint500
                                                    isPartial -> Amber500.copy(alpha = 0.8f)
                                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isAllDone) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Today's Habits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Checklist",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$completedCount/$totalCount done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Habits List or Empty State
            if (scheduledToday.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (habits.isEmpty()) "No habits yet!" else "No habits scheduled for today!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start small, stay consistent, and watch your habits transform your days.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onAddHabit,
                                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Habit")
                                }

                                if (habits.isEmpty()) {
                                    Button(
                                        onClick = onSeedPresets,
                                        colors = ButtonDefaults.buttonColors(containerColor = Mint500)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Load Presets")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(scheduledToday, key = { it.id }) { habit ->
                    val isDone = todayCompletedIds.contains(habit.id)
                    val habitCompletions = allCompletions.filter { it.habitId == habit.id }.map { it.date }.toSet()
                    val streak = HabitAnalytics.calculateCurrentStreak(habit, habitCompletions, today)

                    HabitCard(
                        habit = habit,
                        isCompleted = isDone,
                        streak = streak,
                        onToggleCompletion = { onToggleCompletion(habit.id) },
                        onClickDetail = { onClickDetail(habit) },
                        onEdit = { onEditHabit(habit) },
                        onTogglePause = { onTogglePause(habit.id) },
                        onDelete = { onDeleteHabit(habit.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
