package com.example.accountbook.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.accountbook.local.ExpenseDatabase
import com.example.accountbook.model.Expense
import com.example.accountbook.model.Category
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.repository.ExpenseRepository
import com.example.accountbook.repository.CategoryRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    val expenseRepository: ExpenseRepository
    val categoryRepository: CategoryRepository

    val allExpenses: LiveData<List<Expense>>
    val expensesWithPhotos: LiveData<List<Expense>>

    val allCategories: LiveData<List<Category>>
    val allExpensesWithCategory: LiveData<List<ExpenseWithCategory>>
    val expensesWithPhotosAndCategory: LiveData<List<ExpenseWithCategory>>

    init {
        // Database에서 DAO 가져오기
        val database = ExpenseDatabase.getDatabase(application)
        val expenseDao = database.expenseDao()
        val categoryDao = database.categoryDao()

        // Repository 인스턴스 생성
        expenseRepository = ExpenseRepository(expenseDao)
        categoryRepository = CategoryRepository(categoryDao)

        allExpenses = expenseRepository.allExpenses
        expensesWithPhotos = expenseRepository.getExpensesWithValidPhotos()

        allCategories = categoryRepository.getAllCategories()
        allExpensesWithCategory = expenseRepository.getAllExpensesWithCategory()
        expensesWithPhotosAndCategory = expenseRepository.getExpensesWithPhotosAndCategory()


        viewModelScope.launch {
            categoryRepository.insertDefaultCategories()
        }
    }


    // 지출 추가
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.insertExpense(expense)
    }

    // 지출 수정
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.updateExpense(expense)
    }

    // 지출 삭제
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.deleteExpense(expense)
    }

    // ID로 지출 삭제
    fun deleteExpenseById(id: Long) = viewModelScope.launch {
        expenseRepository.deleteExpenseById(id)
    }

    // 특정 지출 조회
    fun getExpenseById(id: Long): LiveData<Expense?> = liveData {
        emit(expenseRepository.getExpenseById(id))
    }

    // === 새로운 지출 관련 편의 메서드들 ===

    // 지출 카테고리 변경
    fun updateExpenseCategory(expenseId: Long, categoryId: Long?) = viewModelScope.launch {
        expenseRepository.updateExpenseCategory(expenseId, categoryId)
    }

    // 지출 사진 변경
    fun updateExpensePhoto(expenseId: Long, photoUri: String?) = viewModelScope.launch {
        expenseRepository.updateExpensePhoto(expenseId, photoUri)
    }

    // === 카테고리 관련 메서드들 (새로 추가) ===

    // 카테고리 추가
    fun insertCategory(category: Category) = viewModelScope.launch {
        categoryRepository.insertCategory(category)
    }

    // 새 카테고리 생성 (유효성 검사 포함)
    fun createCategory(name: String, iconName: String, colorHex: String) = viewModelScope.launch {
        categoryRepository.createCategory(name, iconName, colorHex)
    }

    // 카테고리 수정
    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryRepository.updateCategory(category)
    }

    // 카테고리 이름 수정
    fun updateCategoryName(categoryId: Long, newName: String) = viewModelScope.launch {
        categoryRepository.updateCategoryName(categoryId, newName)
    }

    // 카테고리 삭제
    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
    }

    // 특정 카테고리 조회
    fun getCategoryById(id: Long): LiveData<Category?> = liveData {
        emit(categoryRepository.getCategoryById(id))
    }

    // 카테고리 이름 중복 체크
    fun isCategoryNameExists(name: String): LiveData<Boolean> = liveData {
        emit(categoryRepository.isCategoryNameExists(name))
    }

    // === 주석 처리된 메서드들을 카테고리 기능과 함께 활성화 ===

    // 카테고리별 지출 조회 (카테고리 정보 포함)
    fun getExpensesByCategory(categoryId: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseRepository.getExpensesByCategory(categoryId)
    }

    // 날짜 범위로 지출 조회 (카테고리 정보 포함)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseRepository.getExpensesByDateRange(startDate, endDate)
    }
}