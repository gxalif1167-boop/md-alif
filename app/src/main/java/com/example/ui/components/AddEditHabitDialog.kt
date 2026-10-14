package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HabitEntity
import com.example.util.DateUtils
import com.example.util.HabitAnalytics

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    habitToEdit: HabitEntity?,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit
) {
    val isEditMode = habitToEdit != null

    var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var description by remember { mutableStateOf(habitToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(habitToEdit?.category ?: HabitIconRegistry.CATEGORIES.first()) }
    var selectedIcon by remember { mutableStateOf(habitToEdit?.icon ?: "sparkles") }
    var selectedColor by remember { mutableStateOf(habitToEdit?.colorHex ?: HabitIconRegistry.COLOR_PALETTE.first()) }
    var frequencyType by remember { mutableStateOf(habitToEdit?.frequencyType ?: "DAILY") }
    var selectedWeekdays by remember {
        val initial = habitToEdit?.weekdays?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet()
            ?: setOf(1, 2, 3, 4, 5, 6, 7)
        mutableStateOf(initial)
    }
    var weeklyTargetDays by remember { mutableIntStateOf(habitToEdit?.weeklyTargetDays ?: 7) }
    var reminderTime by remember { mutableStateOf(habitToEdit?.reminderTime ?: "") }

    var nameError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_habit_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Edit Habit" else "Create New Habit",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets (Only when creating new habit)
                if (!isEditMode) {
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(HabitAnalytics.PRESET_HABITS) { preset ->
                            FilterChip(
                                selected = name == preset.name,
                                onClick = {
                                    name = preset.name
                                    description = preset.description
                                    selectedCategory = preset.category
                                    selectedIcon = preset.icon
                                    selectedColor = preset.colorHex
                                    frequencyType = preset.frequencyType
                                    if (preset.weekdays.isNotBlank()) {
                                        selectedWeekdays = preset.weekdays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                                    }
                                },
                                label = { Text(preset.name, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = HabitIconRegistry.getIcon(preset.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = HabitIconRegistry.parseColor(preset.colorHex)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Habit Name *") },
                    placeholder = { Text("e.g. Read 20 mins, Drink Water") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Habit name is required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (Optional)") },
                    placeholder = { Text("Why this habit matters...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitIconRegistry.CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Selection
                Text(
                    text = "Habit Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(HabitIconRegistry.COLOR_PALETTE) { hex ->
                        val color = HabitIconRegistry.parseColor(hex)
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selection
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(HabitIconRegistry.ICONS) { iconItem ->
                        val isSelected = selectedIcon == iconItem.id
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconItem.id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconItem.icon,
                                contentDescription = iconItem.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Selection
                Text(
                    text = "Schedule & Frequency",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DAILY" to "Every Day", "WEEKDAYS" to "Specific Days").forEach { (type, label) ->
                        FilterChip(
                            selected = frequencyType == type,
                            onClick = { frequencyType = type },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                if (frequencyType == "WEEKDAYS") {
                    Spacer(modifier = Modifier.height(8.dp))
                    val daysLabels = listOf(
                        1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysLabels.forEach { (isoDay, label) ->
                            val isSelected = selectedWeekdays.contains(isoDay)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        selectedWeekdays = if (isSelected) {
                                            if (selectedWeekdays.size > 1) selectedWeekdays - isoDay else selectedWeekdays
                                        } else {
                                            selectedWeekdays + isoDay
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Reminder Input
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Daily Reminder Time (HH:mm)") },
                    placeholder = { Text("e.g. 08:30 or 21:00") },
                    leadingIcon = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val newHabit = HabitEntity(
                                id = habitToEdit?.id ?: 0L,
                                name = name.trim(),
                                description = description.trim(),
                                icon = selectedIcon,
                                colorHex = selectedColor,
                                category = selectedCategory,
                                frequencyType = frequencyType,
                                weekdays = selectedWeekdays.sorted().joinToString(","),
                                weeklyTargetDays = weeklyTargetDays,
                                reminderTime = reminderTime.ifBlank { null },
                                startDate = habitToEdit?.startDate ?: DateUtils.todayDateString(),
                                isPaused = habitToEdit?.isPaused ?: false,
                                createdAt = habitToEdit?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(newHabit)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("save_habit_button")
                    ) {
                        Text(if (isEditMode) "Save Changes" else "Create Habit")
                    }
                }
            }
        }
    }
}
