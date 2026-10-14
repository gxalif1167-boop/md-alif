package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.ui.components.HabitIconRegistry
import com.example.ui.theme.Amber500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Mint500
import com.example.util.DateUtils
import com.example.util.HabitAnalytics

@Composable
fun StatisticsScreen(
    habits: List<HabitEntity>,
    allCompletions: List<HabitCompletionEntity>,
    achievements: List<AchievementEntity>,
    modifier: Modifier = Modifier
) {
    val today = remember { DateUtils.todayDateString() }
    val past7Days = remember { DateUtils.getPastDays(7) }
    val past30Days = remember { DateUtils.getPastDays(30) }

    // Real Calculations
    val totalCompletionsCount = allCompletions.size

    val consistencyScore = remember(habits, allCompletions) {
        HabitAnalytics.calculateConsistencyScore(habits, allCompletions, 30)
    }

    val overallStreak = remember(habits, allCompletions, today) {
        HabitAnalytics.calculateOverallStreak(habits, allCompletions, today)
    }

    // Weekly completion rate
    val weeklyRate = remember(habits, allCompletions, past7Days) {
        var scheduledCount = 0
        var doneCount = 0
        val compSet = allCompletions.map { "${it.habitId}_${it.date}" }.toSet()
        for (d in past7Days) {
            for (h in habits) {
                if (HabitAnalytics.isHabitScheduledForDate(h, d)) {
                    scheduledCount++
                    if (compSet.contains("${h.id}_$d")) doneCount++
                }
            }
        }
        if (scheduledCount > 0) (doneCount * 100 / scheduledCount) else 0
    }

    // Monthly completion rate
    val monthlyRate = remember(habits, allCompletions, past30Days) {
        var scheduledCount = 0
        var doneCount = 0
        val compSet = allCompletions.map { "${it.habitId}_${it.date}" }.toSet()
        for (d in past30Days) {
            for (h in habits) {
                if (HabitAnalytics.isHabitScheduledForDate(h, d)) {
                    scheduledCount++
                    if (compSet.contains("${h.id}_$d")) doneCount++
                }
            }
        }
        if (scheduledCount > 0) (doneCount * 100 / scheduledCount) else 0
    }

    // Best streak across all habits
    val bestStreakAll = remember(habits, allCompletions, today) {
        var maxStreak = 0
        for (h in habits) {
            val dates = allCompletions.filter { it.habitId == h.id }.map { it.date }.toSet()
            val bs = HabitAnalytics.calculateBestStreak(h, dates, today)
            if (bs > maxStreak) maxStreak = bs
        }
        maxStreak
    }

    // Most consistent habit
    val mostConsistentHabit = remember(habits, allCompletions, past30Days) {
        if (habits.isEmpty()) null
        else {
            val compSet = allCompletions.map { "${it.habitId}_${it.date}" }.toSet()
            habits.maxByOrNull { h ->
                var sched = 0
                var done = 0
                for (d in past30Days) {
                    if (HabitAnalytics.isHabitScheduledForDate(h, d)) {
                        sched++
                        if (compSet.contains("${h.id}_$d")) done++
                    }
                }
                if (sched > 0) done.toFloat() / sched else 0f
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Analytics & Insights",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track your discipline and consistency over time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Top 4 Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Consistency",
                        value = "$consistencyScore%",
                        subtitle = "Last 30 days",
                        icon = Icons.Default.TrendingUp,
                        iconColor = Mint500,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Current Streak",
                        value = "$overallStreak d",
                        subtitle = "Best: $bestStreakAll d",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "7-Day Rate",
                        value = "$weeklyRate%",
                        subtitle = "Weekly momentum",
                        icon = Icons.Default.AutoAwesome,
                        iconColor = Indigo600,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Total Done",
                        value = "$totalCompletionsCount",
                        subtitle = "Habit check-ins",
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Weekly Activity Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Percentage of scheduled habits completed each day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7-Day Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        past7Days.forEach { dateStr ->
                            val scheduled = habits.filter { HabitAnalytics.isHabitScheduledForDate(it, dateStr) }
                            val compCount = allCompletions.count { it.date == dateStr && scheduled.any { h -> h.id == it.habitId } }
                            val rate = if (scheduled.isNotEmpty()) (compCount.toFloat() / scheduled.size) else 0f

                            val animatedHeight by animateFloatAsState(
                                targetValue = rate,
                                animationSpec = tween(durationMillis = 600),
                                label = "bar_anim"
                            )

                            val dayLabel = DateUtils.getDayOfWeekShort(dateStr).take(3)
                            val isToday = dateStr == today

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = "${(rate * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((animatedHeight * 70).coerceAtLeast(6f).dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            when {
                                                rate >= 1f -> Mint500
                                                rate > 0f -> Indigo600
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isToday) Indigo600 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Most Consistent Habit Spotlight
        if (mostConsistentHabit != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Amber500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Most Consistent Habit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Amber500
                                )
                            )
                            Text(
                                text = mostConsistentHabit.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Category: ${mostConsistentHabit.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Per-Habit Performance Breakdown
        item {
            Text(
                text = "Habit Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (habits.isEmpty()) {
            item {
                Text(
                    text = "No habits to analyze yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                val habitCompletions = allCompletions.filter { it.habitId == habit.id }
                val compDates = habitCompletions.map { it.date }.toSet()
                val currentStreak = HabitAnalytics.calculateCurrentStreak(habit, compDates, today)
                val bestStreak = HabitAnalytics.calculateBestStreak(habit, compDates, today)

                var scheduledCount = 0
                var doneCount = 0
                for (d in past30Days) {
                    if (HabitAnalytics.isHabitScheduledForDate(habit, d)) {
                        scheduledCount++
                        if (compDates.contains(d)) doneCount++
                    }
                }
                val rate = if (scheduledCount > 0) (doneCount.toFloat() / scheduledCount).coerceIn(0f, 1f) else 0f
                val habitColor = HabitIconRegistry.parseColor(habit.colorHex)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(habitColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = HabitIconRegistry.getIcon(habit.icon),
                                        contentDescription = null,
                                        tint = habitColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(rate * 100).toInt()}% consistency • Streak: $currentStreak d",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${habitCompletions.size} done",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { rate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = habitColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Achievements Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Milestones & Achievements",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Unlock badges by building consistency and breaking streak records",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(achievements, key = { it.id }) { ach ->
            val iconVector = when (ach.icon) {
                "sparkles" -> Icons.Default.AutoAwesome
                "flame" -> Icons.Default.LocalFireDepartment
                "crown" -> Icons.Default.WorkspacePremium
                "trophy" -> Icons.Default.EmojiEvents
                "star" -> Icons.Default.Star
                "shield" -> Icons.Default.Shield
                "check_circle" -> Icons.Default.CheckCircle
                "layers" -> Icons.Default.Layers
                else -> Icons.Default.EmojiEvents
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (ach.isUnlocked) Amber500.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) Amber500 else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ach.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (ach.isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Mint500.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Unlocked",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Mint500
                                    )
                                }
                            } else {
                                Text(
                                    text = "${ach.currentProgress}/${ach.maxProgress}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!ach.isUnlocked && ach.maxProgress > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val progressFloat = (ach.currentProgress.toFloat() / ach.maxProgress).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Amber500,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
