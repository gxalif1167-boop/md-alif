package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AchievementCelebrationDialog
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.HabitDetailDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.HabitFlowTheme
import com.example.ui.viewmodel.HabitViewModel

enum class HabitFlowScreen(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Home),
    HABITS("Habits", Icons.Default.FormatListBulleted),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    STATISTICS("Stats", Icons.Default.TrendingUp),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun HabitFlowApp(viewModel: HabitViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(HabitFlowScreen.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    HabitFlowTheme(themePreference = uiState.preferences.themeMode) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@HabitFlowTheme
        }

        // Onboarding Check
        if (!uiState.preferences.isOnboarded) {
            OnboardingScreen(
                onComplete = { quickStart ->
                    viewModel.completeOnboarding(quickStart)
                }
            )
            return@HabitFlowTheme
        }

        // BackHandler for secondary tabs
        if (currentScreen != HabitFlowScreen.DASHBOARD) {
            BackHandler {
                currentScreen = HabitFlowScreen.DASHBOARD
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    HabitFlowScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { targetScreen ->
                    when (targetScreen) {
                        HabitFlowScreen.DASHBOARD -> DashboardScreen(
                            habits = uiState.habits,
                            allCompletions = uiState.allCompletions,
                            onToggleCompletion = { habitId ->
                                viewModel.toggleHabitCompletion(habitId)
                            },
                            onClickDetail = { habit ->
                                viewModel.openDetailDialog(habit)
                            },
                            onEditHabit = { habit ->
                                viewModel.openEditDialog(habit)
                            },
                            onTogglePause = { habitId ->
                                viewModel.togglePauseHabit(habitId)
                            },
                            onDeleteHabit = { habitId ->
                                viewModel.deleteHabit(habitId)
                            },
                            onAddHabit = {
                                viewModel.openAddDialog()
                            },
                            onSeedPresets = {
                                viewModel.seedPresets()
                            },
                            onNavigateCalendar = { dateStr ->
                                viewModel.setSelectedCalendarDate(dateStr)
                                currentScreen = HabitFlowScreen.CALENDAR
                            }
                        )

                        HabitFlowScreen.HABITS -> HabitsScreen(
                            habits = uiState.habits,
                            allCompletions = uiState.allCompletions,
                            searchQuery = uiState.searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            selectedCategory = uiState.selectedCategory,
                            onCategoryChange = { viewModel.setSelectedCategory(it) },
                            statusFilter = uiState.statusFilter,
                            onStatusChange = { viewModel.setStatusFilter(it) },
                            sortOption = uiState.sortOption,
                            onSortChange = { viewModel.setSortOption(it) },
                            onToggleCompletion = { habitId ->
                                viewModel.toggleHabitCompletion(habitId)
                            },
                            onClickDetail = { habit ->
                                viewModel.openDetailDialog(habit)
                            },
                            onEditHabit = { habit ->
                                viewModel.openEditDialog(habit)
                            },
                            onTogglePause = { habitId ->
                                viewModel.togglePauseHabit(habitId)
                            },
                            onDeleteHabit = { habitId ->
                                viewModel.deleteHabit(habitId)
                            },
                            onAddHabit = {
                                viewModel.openAddDialog()
                            }
                        )

                        HabitFlowScreen.CALENDAR -> CalendarScreen(
                            habits = uiState.habits,
                            allCompletions = uiState.allCompletions,
                            selectedDate = uiState.selectedCalendarDate,
                            onDateSelect = { viewModel.setSelectedCalendarDate(it) },
                            firstDayOfWeek = uiState.preferences.firstDayOfWeek,
                            onToggleCompletion = { habitId, date ->
                                viewModel.toggleHabitCompletion(habitId, date)
                            },
                            onClickDetail = { habit ->
                                viewModel.openDetailDialog(habit)
                            },
                            onEditHabit = { habit ->
                                viewModel.openEditDialog(habit)
                            },
                            onTogglePause = { habitId ->
                                viewModel.togglePauseHabit(habitId)
                            },
                            onDeleteHabit = { habitId ->
                                viewModel.deleteHabit(habitId)
                            }
                        )

                        HabitFlowScreen.STATISTICS -> StatisticsScreen(
                            habits = uiState.habits,
                            allCompletions = uiState.allCompletions,
                            achievements = uiState.achievements
                        )

                        HabitFlowScreen.SETTINGS -> SettingsScreen(
                            preferences = uiState.preferences,
                            onThemeChange = { viewModel.setThemePreference(it) },
                            onFirstDayChange = { viewModel.setFirstDayOfWeek(it) },
                            onReminderChange = { enabled, time ->
                                viewModel.setReminderPreference(enabled, time)
                            },
                            onSeedPresets = { viewModel.seedPresets() },
                            onClearAllData = { viewModel.clearAllData() },
                            onExportJson = { viewModel.exportJson() },
                            onImportJson = { json, onResult ->
                                viewModel.importJson(json, onResult)
                            },
                            onShowToast = { viewModel.showToast(it) }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (uiState.isAddEditOpen) {
            AddEditHabitDialog(
                habitToEdit = uiState.habitToEdit,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { habit ->
                    viewModel.saveHabit(habit)
                }
            )
        }

        uiState.detailHabit?.let { habit ->
            HabitDetailDialog(
                habit = habit,
                completions = uiState.allCompletions,
                onDismiss = { viewModel.closeDetailDialog() },
                onEdit = {
                    viewModel.closeDetailDialog()
                    viewModel.openEditDialog(habit)
                },
                onTogglePause = {
                    viewModel.togglePauseHabit(habit.id)
                },
                onDelete = {
                    viewModel.deleteHabit(habit.id)
                }
            )
        }

        uiState.celebrationAchievement?.let { ach ->
            AchievementCelebrationDialog(
                achievement = ach,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}
