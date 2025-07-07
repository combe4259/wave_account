package com.example.accountbook.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.accountbook.local.ExpenseDao
import com.example.accountbook.model.Expense
import com.example.accountbook.dto.ExpenseWithCategory

class ExpenseRepository(private val expenseDao: ExpenseDao) {


    // 모든 지출 데이터
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses()

    // 사진이 있는 지출만
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

    // 지출 카테고리 변경
    suspend fun updateExpenseCategory(expenseId: Long, categoryId: Long?): Boolean {
        val expense = getExpenseById(expenseId) ?: return false
        val updatedExpense = expense.copy(categoryId = categoryId)
        updateExpense(updatedExpense)
        return true
    }

    // 지출 사진 추가/수정
    suspend fun updateExpensePhoto(expenseId: Long, photoUri: String?): Boolean {
        val expense = getExpenseById(expenseId) ?: return false
        val updatedExpense = expense.copy(photoUri = photoUri)
        updateExpense(updatedExpense)
        return true
    }


    //<-- 카테고리 정보가 포함 -->

    fun getAllExpensesWithCategory(): LiveData<List<ExpenseWithCategory>> {
        return expenseDao.getAllExpensesWithCategory()
    }

    // 사진이 있는 지출들
    fun getExpensesWithPhotosAndCategory(): LiveData<List<ExpenseWithCategory>> {
        return expenseDao.getExpensesWithPhotosAndCategory()
    }

    // 특정 카테고리의 지출들 조회
    fun getExpensesByCategory(categoryId: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseDao.getExpensesByCategory(categoryId)
    }

    // 특정 날짜 범위의 지출들 조회
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }
}