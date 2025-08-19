package com.example.accountbook.domain.repository

import com.example.accountbook.domain.model.Category
import com.example.accountbook.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // Transaction 관련
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    fun getTransactionsWithPhotos(): Flow<List<Transaction.Expense>>
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long, isExpense: Boolean)
    
    // Category 관련
    fun getAllCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: Long)
    suspend fun isCategoryNameExists(name: String, type: com.example.accountbook.domain.model.CategoryType): Boolean
}

interface PreferencesRepository {
    suspend fun getMonthlyBudget(): Int
    suspend fun setMonthlyBudget(amount: Int)
    suspend fun getSelectedTheme(): String
    suspend fun setSelectedTheme(theme: String)
}