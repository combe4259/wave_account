package com.example.accountbook.dto

import java.util.Calendar

data class MonthlyIncomeExpenseData(
    val totalIncome: Double,
    val totalExpense: Double,
    val incomeCount: Int,
    val expenseCount: Int,
    val dailyIncomeTotals: Map<Int, Double>,
    val dailyExpenseTotals: Map<Int, Double>,
    val allIncomes: List<IncomeWithCategory>,
    val allExpenses: List<ExpenseWithCategory>,
    val month: Calendar
)