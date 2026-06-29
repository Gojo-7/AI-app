package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Dining", Icons.Filled.Restaurant, Color(0xFFE57373)),
    SHOPPING("Shopping", Icons.Filled.ShoppingBag, Color(0xFF64B5F6)),
    TRANSPORT("Transport", Icons.Filled.DirectionsCar, Color(0xFF81C784)),
    UTILITIES("Utilities", Icons.Filled.Bolt, Color(0xFFFFD54F)),
    ENTERTAINMENT("Entertainment", Icons.Filled.Movie, Color(0xFFBA68C8)),
    HEALTH("Health", Icons.Filled.MedicalServices, Color(0xFF4DB6AC)),
    OTHERS("Others", Icons.Filled.MoreHoriz, Color(0xFFA1887F));

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: OTHERS
        }
    }
}
