package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.HabitFlowDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.UserPreferenceEntity
import com.example.data.repository.HabitRepository
import com.example.util.DateUtils
import com.example.util.HabitAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitUiState(
    val habits: List<HabitEntity> = emptyList(),
    val allCompletions: List<HabitCompletionEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val preferences: UserPreferenceEntity = UserPreferenceEntity(),
    val selectedCalendarDate: String = DateUtils.todayDateString(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val statusFilter: String = "All", // "All", "Active", "Completed", "Paused"
    val sortOption: String = "RECENT", // "RECENT", "ALPHABETICAL", "STREAK"
    val isAddEditOpen: Boolean = false,
    val habitToEdit: HabitEntity? = null,
    val detailHabit: HabitEntity? = null,
    val celebrationAchievement: AchievementEntity? = null,
    val toastMessage: String? = null,
    val isLoading: Boolean = true
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository
    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    init {
        val database = HabitFlowDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())

        viewModelScope.launch {
            repository.initializeIfEmpty()
        }

        viewModelScope.launch {
            combine(
                repository.allHabits,
                repository.allCompletions,
                repository.allAchievements,
                repository.preferences
            ) { habits, completions, achievements, preferences ->
                _uiState.update { current ->
                    current.copy(
                        habits = habits,
                        allCompletions = completions,
                        achievements = achievements,
                        preferences = preferences ?: UserPreferenceEntity(),
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun toggleHabitCompletion(habitId: Long, date: String = DateUtils.todayDateString()) {
        viewModelScope.launch {
            val isCompleted = repository.toggleCompletion(habitId, date)
            val newlyUnlocked = repository.checkAndUnlockAchievements()
            if (newlyUnlocked.isNotEmpty()) {
                _uiState.update { it.copy(celebrationAchievement = newlyUnlocked.first()) }
            }
            val habit = _uiState.value.habits.find { it.id == habitId }
            val name = habit?.name ?: "Habit"
            showToast(if (isCompleted) "Completed $name! Great job!" else "Unmarked $name")
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            if (habit.id == 0L) {
                repository.insertHabit(habit)
                showToast("Habit '${habit.name}' created!")
            } else {
                repository.updateHabit(habit)
                showToast("Habit updated!")
            }
            closeAddEditDialog()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            _uiState.update { it.copy(detailHabit = null) }
            showToast("Habit deleted")
        }
    }

    fun togglePauseHabit(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitPause(habitId)
            val habit = _uiState.value.habits.find { it.id == habitId }
            val isNowPaused = habit?.isPaused != true
            showToast(if (isNowPaused) "Habit paused" else "Habit resumed")
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddEditOpen = true, habitToEdit = null) }
    }

    fun openEditDialog(habit: HabitEntity) {
        _uiState.update { it.copy(isAddEditOpen = true, habitToEdit = habit) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditOpen = false, habitToEdit = null) }
    }

    fun openDetailDialog(habit: HabitEntity) {
        _uiState.update { it.copy(detailHabit = habit) }
    }

    fun closeDetailDialog() {
        _uiState.update { it.copy(detailHabit = null) }
    }

    fun setSelectedCalendarDate(date: String) {
        _uiState.update { it.copy(selectedCalendarDate = date) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setStatusFilter(status: String) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun setSortOption(sort: String) {
        _uiState.update { it.copy(sortOption = sort) }
    }

    fun setThemePreference(theme: String) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(themeMode = theme)
            repository.savePreferences(updated)
        }
    }

    fun setFirstDayOfWeek(firstDay: String) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(firstDayOfWeek = firstDay)
            repository.savePreferences(updated)
        }
    }

    fun setReminderPreference(enabled: Boolean, time: String) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(
                dailyReminderEnabled = enabled,
                dailyReminderTime = time
            )
            repository.savePreferences(updated)
            showToast(if (enabled) "Daily reminder set for $time" else "Daily reminders disabled")
        }
    }

    fun completeOnboarding(quickStartWithPresets: Boolean) {
        viewModelScope.launch {
            if (quickStartWithPresets) {
                repository.seedPresetHabits()
            }
            val updated = _uiState.value.preferences.copy(isOnboarded = true)
            repository.savePreferences(updated)
        }
    }

    fun seedPresets() {
        viewModelScope.launch {
            repository.seedPresetHabits()
            showToast("Added preset habits!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showToast("All data cleared")
        }
    }

    suspend fun exportJson(): String {
        return repository.exportToJson()
    }

    fun importJson(jsonStr: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonStr)
            result.fold(
                onSuccess = { count ->
                    showToast("Successfully imported $count habits!")
                    onResult(true, "Successfully imported $count habits!")
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Failed to import JSON")
                }
            )
        }
    }

    fun dismissCelebration() {
        _uiState.update { it.copy(celebrationAchievement = null) }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HabitViewModel(application) as T
                }
            }
    }
}
