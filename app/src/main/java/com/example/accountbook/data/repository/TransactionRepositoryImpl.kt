package com.example.accountbook.data.repository

import androidx.lifecycle.asFlow
import com.example.accountbook.data.mapper.*
import com.example.accountbook.domain.model.Category
import com.example.accountbook.domain.model.CategoryType
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.repository.TransactionRepository
import com.example.accountbook.local.ExpenseCategoryDao
import com.example.accountbook.local.ExpenseDao
import com.example.accountbook.local.IncomeCategoryDao
import com.example.accountbook.local.IncomeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val expenseCategoryDao: ExpenseCategoryDao,
    private val incomeCategoryDao: IncomeCategoryDao
) : TransactionRepository {
    
    override fun getAllTransactions(): Flow<List<Transaction>> {
        // LiveData를 Flow로 변환
        val expensesFlow = expenseDao.getAllExpenses().asFlow()
        val incomesFlow = incomeDao.getAllIncomes().asFlow()
        
        return combine(expensesFlow, incomesFlow) { expenses, incomes ->
            val expenseTransactions: List<Transaction> = expenses.map { expense -> 
                expense.toDomain() 
            }
            val incomeTransactions: List<Transaction> = incomes.map { income -> 
                income.toDomain() 
            }
            (expenseTransactions + incomeTransactions).sortedByDescending { it.date }
        }
    }
    
    override fun getTransactionsByDateRange(
        startDate: Long, 
        endDate: Long
    ): Flow<List<Transaction>> {
        // LiveData를 Flow로 변환
        val expensesFlow = expenseDao.getExpensesByDateRange(startDate, endDate).asFlow()
        val incomesFlow = incomeDao.getIncomesByDateRange(startDate, endDate).asFlow()
        
        return combine(expensesFlow, incomesFlow) { expensesWithCategory, incomesWithCategory ->
            val expenseTransactions: List<Transaction> = expensesWithCategory.map { 
                it.toDomain()  // ExpenseWithCategory에 대한 확장 함수 사용
            }
            val incomeTransactions: List<Transaction> = incomesWithCategory.map { 
                it.toDomain()  // IncomeWithCategory에 대한 확장 함수 사용
            }
            (expenseTransactions + incomeTransactions).sortedByDescending { it.date }
        }
    }
    
    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        val expensesFlow = expenseDao.getExpensesByCategory(categoryId).asFlow()
        val incomesFlow = incomeDao.getIncomesByCategory(categoryId).asFlow()
        
        return combine(expensesFlow, incomesFlow) { expensesWithCategory, incomesWithCategory ->
            val expenseTransactions: List<Transaction> = expensesWithCategory.map { 
                it.toDomain()  // ExpenseWithCategory에 대한 확장 함수 사용
            }
            val incomeTransactions: List<Transaction> = incomesWithCategory.map { 
                it.toDomain()  // IncomeWithCategory에 대한 확장 함수 사용
            }
            (expenseTransactions + incomeTransactions).sortedByDescending { it.date }
        }
    }
    
    override fun getTransactionsWithPhotos(): Flow<List<Transaction.Expense>> {
        return expenseDao.getExpensesWithPhotos().asFlow().map { expenses ->
            expenses
                .filter { it.photoUri != null && it.photoUri.isNotBlank() }
                .map { it.toDomain() }
        }
    }
    
    override suspend fun addTransaction(transaction: Transaction): Long {
        return when (transaction) {
            is Transaction.Expense -> expenseDao.insertExpense(transaction.toEntity())
            is Transaction.Income -> incomeDao.insertIncome(transaction.toEntity())
        }
    }
    
    override suspend fun updateTransaction(transaction: Transaction) {
        when (transaction) {
            is Transaction.Expense -> expenseDao.updateExpense(transaction.toEntity())
            is Transaction.Income -> incomeDao.updateIncome(transaction.toEntity())
        }
    }
    
    override suspend fun deleteTransaction(id: Long, isExpense: Boolean) {
        if (isExpense) {
            expenseDao.deleteExpenseById(id)
        } else {
            incomeDao.deleteIncomeById(id)
        }
    }
    
    override fun getAllCategories(): Flow<List<Category>> {
        val expenseCategoriesFlow = expenseCategoryDao.getAllCategories().asFlow()
        val incomeCategoriesFlow = incomeCategoryDao.getAllCategories().asFlow()
        
        return combine(expenseCategoriesFlow, incomeCategoriesFlow) { expenseCategories, incomeCategories ->
            val expenseCats: List<Category> = expenseCategories.map { it.toDomain() }
            val incomeCats: List<Category> = incomeCategories.map { it.toDomain() }
            expenseCats + incomeCats
        }
    }
    
    override suspend fun addCategory(category: Category): Long {
        return when (category.type) {
            CategoryType.EXPENSE -> expenseCategoryDao.insertCategory(category.toExpenseEntity())
            CategoryType.INCOME -> incomeCategoryDao.insertCategory(category.toIncomeEntity())
        }
    }
    
    override suspend fun updateCategory(category: Category) {
        when (category.type) {
            CategoryType.EXPENSE -> expenseCategoryDao.updateCategory(category.toExpenseEntity())
            CategoryType.INCOME -> incomeCategoryDao.updateCategory(category.toIncomeEntity())
        }
    }
    
    override suspend fun deleteCategory(categoryId: Long) {
        // Try both, only one will succeed
        try {
            expenseCategoryDao.deleteCategoryById(categoryId)
        } catch (e: Exception) {
            // Ignore
        }
        try {
            incomeCategoryDao.deleteCategoryById(categoryId)
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    override suspend fun isCategoryNameExists(name: String, type: CategoryType): Boolean {
        return when (type) {
            CategoryType.EXPENSE -> expenseCategoryDao.isCategoryNameExists(name)
            CategoryType.INCOME -> incomeCategoryDao.isCategoryNameExists(name)
        }
    }
}