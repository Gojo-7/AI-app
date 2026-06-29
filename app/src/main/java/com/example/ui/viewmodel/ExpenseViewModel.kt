package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Expense
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    private val prefs = application.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    val allExpenses: StateFlow<List<Expense>>

    // Selected Month filter, format: "yyyy-MM" (e.g., "2026-06")
    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Monthly Budget limit
    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 2000f))
    val monthlyBudget: StateFlow<Float> = _monthlyBudget.asStateFlow()

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)

        // Default to current month
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        _selectedMonth.value = sdf.format(Date())

        allExpenses = repository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Available months list extracted from existing expenses (and always includes current month and selected month)
    val availableMonths: StateFlow<List<String>> = allExpenses.combine(selectedMonth) { list, selected ->
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = sdf.format(Date())
        val months = list.map {
            sdf.format(Date(it.dateMillis))
        }.toMutableSet()
        months.add(currentMonth)
        months.add(selected)
        months.filter { it.isNotEmpty() }.sortedDescending()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
    )

    // Filtered expenses for selected month
    val filteredExpenses: StateFlow<List<Expense>> = combine(allExpenses, selectedMonth) { list, month ->
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        list.filter {
            sdf.format(Date(it.dateMillis)) == month
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Total monthly spending
    val totalSpending: StateFlow<Double> = filteredExpenses.combine(selectedMonth) { list, _ ->
        list.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Category breakdown map: category name -> sum
    val categoryBreakdown: StateFlow<Map<String, Double>> = filteredExpenses.combine(selectedMonth) { list, _ ->
        list.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Daily breakdown for line/bar charts: Day of month (1 to 31) -> sum
    val dailyBreakdown: StateFlow<Map<Int, Double>> = filteredExpenses.combine(selectedMonth) { list, _ ->
        val calendar = Calendar.getInstance()
        list.groupBy {
            calendar.timeInMillis = it.dateMillis
            calendar.get(Calendar.DAY_OF_MONTH)
        }.mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun addExpense(title: String, amount: Double, category: String, dateMillis: Long, notes: String) {
        viewModelScope.launch {
            repository.insert(Expense(title = title, amount = amount, category = category, dateMillis = dateMillis, notes = notes))
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.update(expense)
        }
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setBudget(budget: Float) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat("monthly_budget", budget).apply()
    }
}
