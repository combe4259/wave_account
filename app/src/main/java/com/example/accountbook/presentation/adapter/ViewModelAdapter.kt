package com.example.accountbook.presentation.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountbook.domain.model.Category
import com.example.accountbook.domain.model.CategoryType
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.model.Expense
import com.example.accountbook.model.ExpenseCategory
import com.example.accountbook.model.Income
import com.example.accountbook.model.IncomeCategory
import com.example.accountbook.presentation.main.MainViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MainViewModel을 기존 ExpenseViewModel 인터페이스로 변환하는 어댑터
 * 기존 화면들이 ExpenseViewModel을 사용하도록 되어 있어서 임시로 사용
 */
class ViewModelAdapter(
    private val mainViewModel: MainViewModel
) : ViewModel() {
    
    // 기존 ExpenseViewModel의 프로퍼티들을 MainViewModel에서 가져옴
    val allExpenses: LiveData<List<Expense>> = MutableLiveData(emptyList())
    
    val allExpensesWithCategory: LiveData<List<ExpenseWithCategory>> = 
        mainViewModel.allTransactions
            .map { transactions ->
                transactions
                    .filterIsInstance<Transaction.Expense>()
                    .map { expense ->
                        ExpenseWithCategory(
                            id = expense.id,
                            productName = expense.description,
                            amount = expense.amount,
                            categoryId = expense.categoryId,
                            date = expense.date,
                            photoUri = expense.photoUri,
                            categoryName = null,
                            iconName = null
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            .toLiveData()
    
    val expensesWithPhotos: LiveData<List<Expense>> = 
        mainViewModel.transactionsWithPhotos
            .map { transactions ->
                transactions.map { expense ->
                    Expense(
                        id = expense.id,
                        productName = expense.description,
                        amount = expense.amount,
                        categoryId = expense.categoryId,
                        date = expense.date,
                        photoUri = expense.photoUri
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            .toLiveData()
    
    val expensesWithPhotosAndCategory: LiveData<List<ExpenseWithCategory>> = 
        mainViewModel.transactionsWithPhotos
            .map { transactions ->
                transactions.map { expense ->
                    ExpenseWithCategory(
                        id = expense.id,
                        productName = expense.description,
                        amount = expense.amount,
                        categoryId = expense.categoryId,
                        date = expense.date,
                        photoUri = expense.photoUri,
                        categoryName = null,
                        iconName = null
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            .toLiveData()
    
    val allIncomes: LiveData<List<Income>> = MutableLiveData(emptyList())
    val allIncomesWithCategory: LiveData<List<IncomeWithCategory>> = MutableLiveData(emptyList())
    
    val allExpenseCategories: LiveData<List<ExpenseCategory>> = 
        mainViewModel.expenseCategories
            .map { categories ->
                categories.map { category ->
                    ExpenseCategory(
                        id = category.id,
                        name = category.name,
                        iconName = category.iconName
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            .toLiveData()
    
    val allIncomeCategories: LiveData<List<IncomeCategory>> = 
        mainViewModel.incomeCategories
            .map { categories ->
                categories.map { category ->
                    IncomeCategory(
                        id = category.id,
                        name = category.name,
                        iconName = category.iconName
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            .toLiveData()
    
    // 메서드들
    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            val transaction = Transaction.Expense(
                id = expense.id,
                amount = expense.amount,
                categoryId = expense.categoryId,
                date = expense.date,
                description = expense.productName,
                photoUri = expense.photoUri
            )
            mainViewModel.addNewTransaction(transaction)
        }
    }
    
    fun insertIncome(income: Income) {
        viewModelScope.launch {
            val transaction = Transaction.Income(
                id = income.id,
                amount = income.amount,
                categoryId = income.categoryId,
                date = income.date,
                description = income.description
            )
            mainViewModel.addNewTransaction(transaction)
        }
    }
    
    fun deleteExpenseById(id: Long) {
        mainViewModel.deleteTransaction(id, true)
    }
    
    fun deleteExpense(expense: Expense) {
        mainViewModel.deleteTransaction(expense.id, true)
    }
    
    fun deleteIncome(income: Income) {
        mainViewModel.deleteTransaction(income.id, false)
    }
    
    fun insertExpenseCategory(category: ExpenseCategory) {
        val domainCategory = Category(
            id = category.id,
            name = category.name,
            iconName = category.iconName,
            type = CategoryType.EXPENSE
        )
        mainViewModel.addCategory(domainCategory)
    }
    
    fun insertIncomeCategory(category: IncomeCategory) {
        val domainCategory = Category(
            id = category.id,
            name = category.name,
            iconName = category.iconName,
            type = CategoryType.INCOME
        )
        mainViewModel.addCategory(domainCategory)
    }
    
    // Extension function to convert StateFlow to LiveData
    private fun <T> kotlinx.coroutines.flow.StateFlow<T>.toLiveData(): LiveData<T> {
        val liveData = MutableLiveData<T>()
        viewModelScope.launch {
            collect { value ->
                liveData.postValue(value)
            }
        }
        return liveData
    }
}