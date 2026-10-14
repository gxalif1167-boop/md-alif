package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconItem(val id: String, val name: String, val icon: ImageVector)

object HabitIconRegistry {
    val ICONS = listOf(
        HabitIconItem("water", "Water", Icons.Default.WaterDrop),
        HabitIconItem("book", "Book", Icons.Default.MenuBook),
        HabitIconItem("exercise", "Fitness", Icons.Default.FitnessCenter),
        HabitIconItem("meditate", "Mindfulness", Icons.Default.SelfImprovement),
        HabitIconItem("sleep", "Sleep", Icons.Default.Bedtime),
        HabitIconItem("study", "Study", Icons.Default.School),
        HabitIconItem("journal", "Journal", Icons.Default.EditNote),
        HabitIconItem("walk", "Walk", Icons.Default.DirectionsWalk),
        HabitIconItem("heart", "Health", Icons.Default.Favorite),
        HabitIconItem("code", "Work", Icons.Default.Terminal),
        HabitIconItem("sun", "Morning", Icons.Default.WbSunny),
        HabitIconItem("sparkles", "Routine", Icons.Default.AutoAwesome)
    )

    fun getIcon(id: String): ImageVector {
        return ICONS.find { it.id == id }?.icon ?: Icons.Default.AutoAwesome
    }

    val COLOR_PALETTE = listOf(
        "#4F46E5", // Indigo
        "#10B981", // Mint
        "#0EA5E9", // Sky Blue
        "#F59E0B", // Amber
        "#EC4899", // Pink
        "#8B5CF6", // Purple
        "#EF4444", // Red
        "#14B8A6"  // Teal
    )

    val CATEGORIES = listOf(
        "Health & Fitness",
        "Mindfulness",
        "Productivity",
        "Learning",
        "Personal",
        "Lifestyle"
    )

    fun parseColor(hex: String, fallback: Color = Color(0xFF4F46E5)): Color {
        return try {
            val cleanHex = hex.replace("#", "")
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(colorInt or 0x00000000FF000000)
            } else {
                Color(colorInt)
            }
        } catch (e: Exception) {
            fallback
        }
    }
}
