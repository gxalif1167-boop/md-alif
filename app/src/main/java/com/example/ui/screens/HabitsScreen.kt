package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitIconRegistry
import com.example.ui.theme.Indigo600
import com.example.util.DateUtils
import com.example.util.HabitAnalytics

@Composable
fun HabitsScreen(
    habits: List<HabitEntity>,
    allCompletions: List<HabitCompletionEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    statusFilter: String,
    onStatusChange: (String) -> Unit,
    sortOption: String,
    onSortChange: (String) -> Unit,
    onToggleCompletion: (Long) -> Unit,
    onClickDetail: (HabitEntity) -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onTogglePause: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onAddHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { DateUtils.todayDateString() }
    val todayCompletedIds = remember(allCompletions, today) {
        allCompletions.filter { it.date == today }.map { it.habitId }.toSet()
    }

    var sortMenuOpen by remember { mutableStateOf(false) }

    // Filter & Sort logic
    val filteredHabits = remember(habits, searchQuery, selectedCategory, statusFilter, sortOption, todayCompletedIds) {
        var list = habits

        // Search query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) || it.description.lowercase().contains(q) || it.category.lowercase().contains(q)
            }
        }

        // Category filter
        if (selectedCategory != "All") {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        // Status filter
        when (statusFilter) {
            "Active" -> list = list.filter { !it.isPaused }
            "Paused" -> list = list.filter { it.isPaused }
            "Completed" -> list = list.filter { todayCompletedIds.contains(it.id) }
            "Pending" -> list = list.filter { !todayCompletedIds.contains(it.id) && !it.isPaused }
        }

        // Sort option
        when (sortOption) {
            "ALPHABETICAL" -> list.sortedBy { it.name.lowercase() }
            "STREAK" -> list.sortedByDescending { h ->
                val compDates = allCompletions.filter { it.habitId == h.id }.map { it.date }.toSet()
                HabitAnalytics.calculateCurrentStreak(h, compDates, today)
            }
            else -> list.sortedByDescending { it.createdAt }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("habits_add_fab")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Habits",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${filteredHabits.size} habit${if (filteredHabits.size != 1) "s" else ""} shown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Sort button with dropdown
                    Box {
                        Button(
                            onClick = { sortMenuOpen = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (sortOption) {
                                    "ALPHABETICAL" -> "A-Z"
                                    "STREAK" -> "Streak"
                                    else -> "Recent"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Added") },
                                onClick = {
                                    onSortChange("RECENT")
                                    sortMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical (A-Z)") },
                                onClick = {
                                    onSortChange("ALPHABETICAL")
                                    sortMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Current Streak") },
                                onClick = {
                                    onSortChange("STREAK")
                                    sortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search habits or categories...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habits_search_input")
                )
            }

            // Category Chips Row
            item {
                val categories = listOf("All") + HabitIconRegistry.CATEGORIES
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Status Filter Chips Row
            item {
                val statuses = listOf("All", "Active", "Completed", "Pending", "Paused")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statuses) { status ->
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { onStatusChange(status) },
                            label = { Text(status, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Habits List or Empty
            if (filteredHabits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No habits found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onSearchChange("")
                                onCategoryChange("All")
                                onStatusChange("All")
                            }
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { it.id }) { habit ->
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
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
