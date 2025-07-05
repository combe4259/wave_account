package com.example.accountbook.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.accountbook.local.ExpenseDao
import com.example.accountbook.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    // 모든 지출 데이터
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses()
//    fun getAllExpenses(): LiveData<List<Expense>> {
//        return expenseDao.getAllExpenses()
//    }

    // 사진이 있는 지출만
//    val expensesWithPhotos: LiveData<List<Expense>> = expenseDao.getExpensesWithPhotos()
    fun getExpensesWithValidPhotos(): LiveData<List<Expense>> {
        return expenseDao.getExpensesWithPhotos().map { expenses ->
            expenses.filter { expense ->
                // photoUri가 유효한 형태인지 검증
                expense.photoUri?.let { uri ->
                    uri.isNotBlank() && (uri.startsWith("content://") || uri.startsWith("file://"))
                } ?: false
            }
        }
    }

    // 지출 추가
    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    // 지출 수정
    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    // 지출 삭제
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    // ID로 지출 삭제
    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    // 특정 지출 조회
    suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)
    }

//    // 카테고리별 지출 조회
//    fun getExpensesByCategory(category: String): LiveData<List<Expense>> {
//        return expenseDao.getExpensesByCategory(category)
//    }
//
//    // 날짜 범위로 지출 조회
//    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
//        return expenseDao.getExpensesByDateRange(startDate, endDate)
//    }
}