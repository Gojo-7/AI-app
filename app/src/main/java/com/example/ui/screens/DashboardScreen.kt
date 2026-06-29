package com.example.ui.screens

import kotlin.math.atan2
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.Expense
import com.example.ui.components.AddEditExpenseDialog
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.ExpenseCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    // Collect Flow states from ViewModel
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val totalSpending by viewModel.totalSpending.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val dailyBreakdown by viewModel.dailyBreakdown.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()

    // Dialog state controllers
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    // Search filter state
    var searchQuery by remember { mutableStateOf("") }

    val localFilteredExpenses = remember(filteredExpenses, searchQuery) {
        if (searchQuery.isBlank()) {
            filteredExpenses
        } else {
            filteredExpenses.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.notes.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 0.dp // Custom bottom handling below
                )
        ) {
            val selectedMonthLabel = remember(selectedMonth) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMMM", Locale.getDefault()) // e.g. "September"
                    val date = inputFormat.parse(selectedMonth)
                    if (date != null) "${outputFormat.format(date)} Overview" else "Overview"
                } catch (e: Exception) {
                    "Overview"
                }
            }

            // Screen Header Title in Bento style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = selectedMonthLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp
                    )
                }

                // Avatar container with initials on the right
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSystemInDarkTheme()) BentoHeroDark else Color(0xFFEADDFF))
                        .clickable { showEditBudgetDialog = true }
                        .border(1.dp, if (isSystemInDarkTheme()) Color(0xFFEADDFF).copy(alpha = 0.2f) else Color(0xFFD0BCFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RR",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) BentoHeroTextDark else Color(0xFF21005D),
                        fontSize = 14.sp
                    )
                }
            }

            // Scrollable contents via LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item 1: Budget Card Banner with interactive progress indicator (Bento Hero)
                item {
                    BudgetProgressBanner(
                        totalSpending = totalSpending,
                        monthlyBudget = monthlyBudget,
                        onEditBudgetClick = { showEditBudgetDialog = true }
                    )
                }

                // Item 2: Horizontal Scrolling Months Bar
                item {
                    MonthSelectorRow(
                        availableMonths = availableMonths,
                        selectedMonth = selectedMonth,
                        onMonthSelected = { viewModel.selectMonth(it) }
                    )
                }

                // Item 3: Bento Grid Section (Pie + Bar Chart Card arranged as Bento)
                item {
                    BentoGridSection(
                        categoryBreakdown = categoryBreakdown,
                        dailyBreakdown = dailyBreakdown,
                        selectedMonth = selectedMonth,
                        totalSpending = totalSpending
                    )
                }

                // Item 4: Sticky Search & Header Area
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Modern Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search transactions...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_field")
                        )
                    }
                }

                // Item 5: Expenses List Items
                if (localFilteredExpenses.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "No transactions found" else "No search matches",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (searchQuery.isEmpty()) "Keep your finances clean!" else "Try entering a different query",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    items(localFilteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onEditClick = { expenseToEdit = expense },
                            onDeleteClick = { viewModel.deleteExpense(expense) }
                        )
                    }
                }

                // Spacer at the bottom to offset the FloatingActionButton
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Dialog: Add Expense
    if (showAddDialog) {
        AddEditExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amt, cat, dateMs, note ->
                viewModel.addExpense(title, amt, cat, dateMs, note)
                showAddDialog = false
            }
        )
    }

    // Dialog: Edit Expense
    expenseToEdit?.let { expense ->
        AddEditExpenseDialog(
            expenseToEdit = expense,
            onDismiss = { expenseToEdit = null },
            onConfirm = { title, amt, cat, dateMs, note ->
                viewModel.updateExpense(expense.copy(title = title, amount = amt, category = cat, dateMillis = dateMs, notes = note))
                expenseToEdit = null
            }
        )
    }

    // Dialog: Edit Monthly Budget
    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showEditBudgetDialog = false },
            onConfirm = { limit ->
                viewModel.setBudget(limit)
                showEditBudgetDialog = false
            }
        )
    }
}

@Composable
fun BudgetProgressBanner(
    totalSpending: Double,
    monthlyBudget: Float,
    onEditBudgetClick: () -> Unit
) {
    val remaining = (monthlyBudget - totalSpending).coerceAtLeast(0.0)
    val fraction = if (monthlyBudget > 0) (totalSpending / monthlyBudget).toFloat() else 0f
    val progress = fraction.coerceIn(0f, 1f)

    // Animated fraction progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "budget_progress"
    )

    val isDark = isSystemInDarkTheme()
    val bannerBg = if (isDark) BentoHeroDark else BentoHeroLight
    val bannerText = if (isDark) BentoHeroTextDark else BentoHeroTextLight

    // Alert color depends on threshold
    val budgetAlertColor = when {
        fraction >= 1.0f -> Color(0xFFBA1A1A)     // Exceeded budget (vibrant Material Red)
        fraction >= 0.85f -> Color(0xFFE28413)    // Warning budget (vibrant orange-amber)
        else -> MaterialTheme.colorScheme.primary // Great shape (Bento primary purple)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_progress_banner"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = bannerBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEditBudgetClick() }
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "TOTAL SPENDING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = bannerText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("$%.2f", totalSpending),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = bannerText,
                            fontSize = 38.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("of $%.0f", monthlyBudget),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = bannerText.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // A beautiful badge: like "↑ 12% vs last month" or "100% remaining"
                    val percentageOfBudget = if (monthlyBudget > 0) (totalSpending / monthlyBudget * 100).toInt() else 0
                    val badgeBg = if (isDark) BentoItem1Dark else BentoItem1Light
                    val badgeText = if (isDark) BentoItem1TextDark else BentoItem1TextLight
                    val alertText = if (percentageOfBudget > 100) {
                        "Exceeded by ${(percentageOfBudget - 100)}%"
                    } else {
                        "${100 - percentageOfBudget}% left"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = alertText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Beautiful custom linear progress bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = budgetAlertColor,
                    trackColor = bannerText.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category.uppercase()) {
        "FOOD", "DINING" -> "🍔"
        "SHOPPING" -> "🛒"
        "TRANSPORT" -> "🚗"
        "UTILITIES" -> "💡"
        "ENTERTAINMENT" -> "🎬"
        "HEALTH" -> "🏥"
        else -> "📦"
    }
}

@Composable
fun BentoGridSection(
    categoryBreakdown: Map<String, Double>,
    dailyBreakdown: Map<Int, Double>,
    selectedMonth: String,
    totalSpending: Double,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val sortedCategories = remember(categoryBreakdown) {
        categoryBreakdown.toList().sortedByDescending { it.second }
    }
    val topCategory = sortedCategories.firstOrNull()
    val topCategoryName = topCategory?.first ?: "None"
    val topCategoryAmount = topCategory?.second ?: 0.0
    val topCategoryEmoji = remember(topCategoryName) {
        getCategoryEmoji(topCategoryName)
    }

    // Daily average calculation based on active days in breakdown
    val activeDaysCount = dailyBreakdown.keys.size.coerceAtLeast(1)
    val dailyAverage = if (totalSpending > 0) totalSpending / activeDaysCount else 0.0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column (Top Category Card + Daily Average Card)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Top Category Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) BentoItem1Dark else BentoItem1Light
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) BentoHeroDark.copy(alpha = 0.5f) else Color(0xFFD0BCFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = topCategoryEmoji,
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Top Sector",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (topCategoryName == "None") "No Expenses" else topCategoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) BentoItem1TextDark else BentoItem1TextLight,
                            maxLines = 1
                        )
                        Text(
                            text = String.format("$%.2f", topCategoryAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Card 2: Daily Average Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) BentoItem2Dark else BentoItem2Light
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Average",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("$%.2f", dailyAverage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) BentoItem2TextDark else BentoItem2TextLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Right Column: Interactive Trends Tall Card (Spans two rows)
        var selectedSubTab by remember { mutableStateOf(0) } // 0 = Pie breakdown, 1 = Weekly Trend

        Card(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedSubTab == 0) "Breakdown" else "Weekly Trend",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        fontSize = 12.sp
                    )

                    // Pill tab toggle selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("Pie", "Trend").forEachIndexed { index, title ->
                            val isSelected = selectedSubTab == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedSubTab = index }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (categoryBreakdown.isEmpty()) {
                        Text(
                            text = "No recorded data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        if (selectedSubTab == 0) {
                            CompactDonutChart(categoryBreakdown = categoryBreakdown)
                        } else {
                            CompactTrendBarChart(dailyBreakdown = dailyBreakdown)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactDonutChart(
    categoryBreakdown: Map<String, Double>
) {
    val totalSpend = categoryBreakdown.values.sum()
    val sortedCategories = remember(categoryBreakdown) {
        categoryBreakdown.toList().sortedByDescending { it.second }
    }

    var selectedCategoryName by remember(categoryBreakdown) {
        mutableStateOf(sortedCategories.firstOrNull()?.first)
    }
    val selectedAmount = categoryBreakdown[selectedCategoryName] ?: 0.0

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sortedCategories, totalSpend) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        var tapAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                        if (tapAngle < 0) tapAngle += 360.0

                        var currentAngle = 0.0
                        for (item in sortedCategories) {
                            val sweep = (item.second / totalSpend) * 360.0
                            if (tapAngle >= currentAngle && tapAngle <= currentAngle + sweep) {
                                selectedCategoryName = item.first
                                break
                            }
                            currentAngle += sweep
                        }
                    }
                }
        ) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            var currentAngle = 0f
            for (item in sortedCategories) {
                val category = ExpenseCategory.fromString(item.first)
                val rawSweep = (item.second / totalSpend).toFloat() * 360f
                val isSelected = category.displayName == selectedCategoryName || category.name == selectedCategoryName
                val actualStroke = if (isSelected) strokeWidth * 1.35f else strokeWidth

                drawArc(
                    color = category.color,
                    startAngle = currentAngle,
                    sweepAngle = rawSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = actualStroke, cap = StrokeCap.Round)
                )
                currentAngle += rawSweep
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            selectedCategoryName?.let { catName ->
                val cat = ExpenseCategory.fromString(catName)
                Text(
                    text = cat.displayName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = String.format("$%.0f", selectedAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CompactTrendBarChart(
    dailyBreakdown: Map<Int, Double>
) {
    // Generate simple 5 bars representing recent active segments (weeks W1-W5)
    val last5Days = remember(dailyBreakdown) {
        val segments = listOf(1..6, 7..12, 13..18, 19..24, 25..31)
        segments.mapIndexed { index, range ->
            val sum = range.sumOf { dailyBreakdown[it] ?: 0.0 }
            val label = when (index) {
                0 -> "W1"
                1 -> "W2"
                2 -> "W3"
                3 -> "W4"
                else -> "W5"
            }
            label to sum
        }
    }

    val maxVal = remember(last5Days) {
        last5Days.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            last5Days.forEach { (label, value) ->
                val ratio = (value / maxVal).toFloat()
                val isHighest = value == last5Days.maxOf { it.second } && value > 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height((ratio * 100).dp.coerceAtLeast(6.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (value > 0) {
                                    if (isHighest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthSelectorRow(
    availableMonths: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableMonths.forEach { month ->
            val isSelected = month == selectedMonth

            // Parse formatted human-friendly month string (e.g. "Jun 2026")
            val displayLabel = remember(month) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    val date = inputFormat.parse(month)
                    if (date != null) outputFormat.format(date) else month
                } catch (e: Exception) {
                    month
                }
            }

            val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val tc = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            val border = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .clickable { onMonthSelected(month) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = tc,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = tc
                )
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val category = remember(expense.category) { ExpenseCategory.fromString(expense.category) }
    var expanded by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize()
            .testTag("expense_card_${expense.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon container
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.displayName,
                            tint = category.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = dateFormatter.format(Date(expense.dateMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Expense amount bold
                Text(
                    text = String.format("-$%.2f", expense.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = category.color,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Expanded detail section
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Divider
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category text
                    Text(
                        text = "Category: ${category.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Notes (if exists)
                    if (expense.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: ${expense.notes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Edit & Delete Action Row with beautiful target sizes (48dp+)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEditClick() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit transaction",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Delete button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDeleteClick() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete transaction",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
