package com.example.accountbook.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.accountbook.local.ExpenseDatabase
import com.example.accountbook.model.Expense
import com.example.accountbook.repository.ExpenseRepository
import kotlinx.coroutines.launch


class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    val repository: ExpenseRepository
    val allExpenses: LiveData<List<Expense>>
    val expensesWithPhotos: LiveData<List<Expense>>

    init {
        val expenseDao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        allExpenses = repository.allExpenses
        expensesWithPhotos = repository.getExpensesWithValidPhotos()
    }

    // 지출 추가
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    // 지출 수정
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    // 지출 삭제
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // ID로 지출 삭제
    fun deleteExpenseById(id: Long) = viewModelScope.launch {
        repository.deleteExpenseById(id)
    }

    // 특정 지출 조회
    fun getExpenseById(id: Long): LiveData<Expense?> = liveData {
        emit(repository.getExpenseById(id))
    }

//    // 카테고리별 지출 조회
//    fun getExpensesByCategory(category: String): LiveData<List<Expense>> {
//        return repository.getExpensesByCategory(category)
//    }
//
//    // 날짜 범위로 지출 조회
//    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
//        return repository.getExpensesByDateRange(startDate, endDate)
//    }
}