package com.example.accountbook.domain.usecase.statistics

import com.example.accountbook.domain.model.Category
import com.example.accountbook.domain.model.DailyTotal
import com.example.accountbook.domain.model.MonthlyStatistics
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.repository.TransactionRepository
import com.example.accountbook.domain.usecase.transaction.GetMonthlyTransactionsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetMonthlyStatisticsUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val getMonthlyTransactions: GetMonthlyTransactionsUseCase
) {
    operator fun invoke(year: Int, month: Int): Flow<MonthlyStatistics> {
        return combine(
            getMonthlyTransactions(year, month),
            repository.getAllCategories()
        ) { transactions, categories ->
            val totalIncome = transactions
                .filterIsInstance<Transaction.Income>()
                .sumOf { it.amount }
            
            val totalExpense = transactions
                .filterIsInstance<Transaction.Expense>()
                .sumOf { it.amount }
            
            val byCategory = calculateByCategory(transactions, categories)
            val dailyTotals = calculateDailyTotals(transactions)
            
            MonthlyStatistics(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                byCategory = byCategory,
                dailyTotals = dailyTotals
            )
        }
    }
    
    private fun calculateByCategory(
        transactions: List<Transaction>,
        categories: List<Category>
    ): Map<Category, Double> {
        val categoryMap = categories.associateBy { it.id }
        return transactions
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, trans) ->
                categoryId?.let { id ->
                    categoryMap[id]?.let { category ->
                        category to trans.sumOf { it.amount }
                    }
                }
            }
            .toMap()
    }
    
    private fun calculateDailyTotals(transactions: List<Transaction>): Map<Int, DailyTotal> {
        val calendar = Calendar.getInstance()
        return transactions
            .groupBy { transaction ->
                calendar.timeInMillis = transaction.date
                calendar.get(Calendar.DAY_OF_MONTH)
            }
            .mapValues { (day, dayTransactions) ->
                DailyTotal(
                    day = day,
                    income = dayTransactions
                        .filterIsInstance<Transaction.Income>()
                        .sumOf { it.amount },
                    expense = dayTransactions
                        .filterIsInstance<Transaction.Expense>()
                        .sumOf { it.amount }
                )
            }
    }
}