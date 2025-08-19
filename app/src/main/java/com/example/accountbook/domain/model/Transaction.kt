package com.example.accountbook.domain.model

sealed interface Transaction {
    val id: Long
    val amount: Double
    val categoryId: Long?
    val date: Long
    val description: String
    
    data class Income(
        override val id: Long = 0,
        override val amount: Double,
        override val categoryId: Long? = null,
        override val date: Long,
        override val description: String
    ) : Transaction
    
    data class Expense(
        override val id: Long = 0,
        override val amount: Double,
        override val categoryId: Long? = null,
        override val date: Long,
        override val description: String,
        val photoUri: String? = null
    ) : Transaction
}

data class Category(
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val type: CategoryType
)

enum class CategoryType {
    INCOME, EXPENSE
}

data class MonthlyStatistics(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double = totalIncome - totalExpense,
    val byCategory: Map<Category, Double>,
    val dailyTotals: Map<Int, DailyTotal>
)

data class DailyTotal(
    val day: Int,
    val income: Double,
    val expense: Double
)