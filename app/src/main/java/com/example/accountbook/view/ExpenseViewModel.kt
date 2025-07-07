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

    // Repository
    private val expenseRepository: ExpenseRepository
    private val expenseCategoryRepository: ExpenseCategoryRepository
    private val incomeRepository: IncomeRepository
    private val incomeCategoryRepository: IncomeCategoryRepository


    //지출

    val allExpenses: LiveData<List<Expense>>
    val expensesWithPhotos: LiveData<List<Expense>>

    val allCategories: LiveData<List<Category>>
    val allExpensesWithCategory: LiveData<List<ExpenseWithCategory>>
    val expensesWithPhotosAndCategory: LiveData<List<ExpenseWithCategory>>


    //소득
    val allIncomes: LiveData<List<Income>>
    val allIncomesWithCategory: LiveData<List<IncomeWithCategory>>

    //카테고리
    val allExpenseCategories: LiveData<List<ExpenseCategory>>
    val allIncomeCategories: LiveData<List<IncomeCategory>>

    init {

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


        allExpenseCategories = expenseCategoryRepository.getAllCategories()
        allIncomeCategories = incomeCategoryRepository.getAllCategories()

        //기본 카테고리 생성
        viewModelScope.launch {
            categoryRepository.insertDefaultCategories()
        }
    }

    // 지출 관련 메서드들

    /**
     * 새로운 지출 추가
     */
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.insertExpense(expense)
    }

    /**
     * 기존 지출 정보 수정
     */
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.updateExpense(expense)
    }

    /**
     * 지출 삭제
     */
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.deleteExpense(expense)
    }


    /**
     * ID로 지출 삭제
     */
    fun deleteExpenseById(id: Long) = viewModelScope.launch {
        expenseRepository.deleteExpenseById(id)
    }

    /**
     * 특정 ID의 지출 조회
     */
    fun getExpenseById(id: Long): LiveData<Expense?> = liveData {
        emit(expenseRepository.getExpenseById(id))
    }

    /**
     * 지출 카테고리 변경
     */
    fun updateExpenseCategory(expenseId: Long, categoryId: Long?) = viewModelScope.launch {
        expenseRepository.updateExpenseCategory(expenseId, categoryId)
    }

    /**
     * 지출 사진 추가 변경
     */
    fun updateExpensePhoto(expenseId: Long, photoUri: String?) = viewModelScope.launch {
        expenseRepository.updateExpensePhoto(expenseId, photoUri)
    }


    // 소득 관련 메서드들

    /**
     * 새로운 소득 추가
     */
    fun insertIncome(income: Income) = viewModelScope.launch {
        incomeRepository.insertIncome(income)
    }

    /**
     * 기존 소득 수정
     */
    fun updateIncome(income: Income) = viewModelScope.launch {
        incomeRepository.updateIncome(income)
    }

    /**
     * 소득 삭제
     */
    fun deleteIncome(income: Income) = viewModelScope.launch {
        incomeRepository.deleteIncome(income)
    }

    // 지출 카테고리 관리 메서드들

    /**
     * 새로운 지출 카테고리 추가
     */
    fun insertExpenseCategory(category: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.insertCategory(category)
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


    /**
     * 특정 ID의 지출 카테고리를 조회합니다
     * @param id 조회할 카테고리의 ID
     * @return 카테고리 객체를 담은 LiveData (없으면 null)
     */
    fun getExpenseCategoryById(id: Long): LiveData<ExpenseCategory?> = liveData {
        emit(expenseCategoryRepository.getCategoryById(id))
    }

    /**
     * 지출 카테고리 이름 중복을 체크합니다
     */
    fun isExpenseCategoryNameExists(name: String): LiveData<Boolean> = liveData {
        emit(expenseCategoryRepository.isCategoryNameExists(name))
    }

    // 소득 카테고리 관리 메서드들

//    /**
//     * 새로운 소득 카테고리를 추가합니다
//     */
//    fun insertIncomeCategory(category: IncomeCategory) = viewModelScope.launch {
//        incomeCategoryRepository.insertCategory(category)
//    }

    /**
     * 소득 카테고리 정보를 수정
     */
    fun updateIncomeCategory(category: IncomeCategory) = viewModelScope.launch {
        incomeCategoryRepository.updateCategory(category)
    }

    /**
     * 소득 카테고리를 삭제
     */
    fun deleteIncomeCategory(category: IncomeCategory) = viewModelScope.launch {
        incomeCategoryRepository.deleteCategory(category)
    }

    /**
     * 소득 카테고리 이름 중복을 체크
     */
    fun isIncomeCategoryNameExists(name: String): LiveData<Boolean> = liveData {
        emit(incomeCategoryRepository.isCategoryNameExists(name))
    }

    // 조회 및 분석 메서드들

    /**
     * 특정 카테고리의 지출들을 조회
     */
    fun getExpensesByCategory(categoryId: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseRepository.getExpensesByCategory(categoryId)
    }

    /**
     * 특정 날짜 범위의 지출들을 조회합
     */
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseRepository.getExpensesByDateRange(startDate, endDate)
    }

}