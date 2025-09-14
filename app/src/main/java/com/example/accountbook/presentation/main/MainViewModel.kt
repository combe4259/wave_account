package com.example.accountbook.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountbook.domain.model.MonthlyStatistics
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.model.Category
import com.example.accountbook.domain.usecase.category.GetAllCategoriesUseCase
import com.example.accountbook.domain.usecase.statistics.GetMonthlyStatisticsUseCase
import com.example.accountbook.domain.usecase.transaction.AddTransactionUseCase
import com.example.accountbook.domain.usecase.transaction.GetAllTransactionsUseCase
import com.example.accountbook.domain.usecase.transaction.GetMonthlyTransactionsUseCase
import com.example.accountbook.domain.repository.TransactionRepository
import com.example.accountbook.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val getAllTransactions: GetAllTransactionsUseCase,
    private val getMonthlyTransactions: GetMonthlyTransactionsUseCase,
    private val getMonthlyStatistics: GetMonthlyStatisticsUseCase,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // UI State - 화면 상태만 관리
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // 월 목표 금액 - SavedStateHandle로 관리
    var monthlyBudget: Int
        get() = savedStateHandle.get<Int>("monthly_budget") ?: 1_000_000
        set(value) {
            savedStateHandle["monthly_budget"] = value
            _uiState.update { it.copy(monthlyBudget = value) }
        }
    
    // 현재 선택된 년월
    private val _selectedYearMonth = MutableStateFlow(
        Calendar.getInstance().let { it.get(Calendar.YEAR) to it.get(Calendar.MONTH) }
    )
    
    // 월별 거래 내역
    val monthlyTransactions: StateFlow<List<Transaction>> = _selectedYearMonth
        .flatMapLatest { (year, month) ->
            getMonthlyTransactions(year, month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 월별 통계
    val monthlyStatistics: StateFlow<MonthlyStatistics?> = _selectedYearMonth
        .flatMapLatest { (year, month) ->
            getMonthlyStatistics(year, month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // 전체 거래 내역 (갤러리, 통계 화면용)
    val allTransactions: StateFlow<List<Transaction>> = getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 사진이 있는 거래만
    val transactionsWithPhotos: StateFlow<List<Transaction.Expense>> = 
        repository.getTransactionsWithPhotos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    // 모든 카테고리
    val allCategories: StateFlow<List<Category>> = getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 지출 카테고리만
    val expenseCategories: StateFlow<List<Category>> = allCategories
        .map { categories ->
            categories.filter { it.type == com.example.accountbook.domain.model.CategoryType.EXPENSE }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 수입 카테고리만
    val incomeCategories: StateFlow<List<Category>> = allCategories
        .map { categories ->
            categories.filter { it.type == com.example.accountbook.domain.model.CategoryType.INCOME }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // UI 이벤트 (SnackBar, Dialog 등)
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    
    init {
        // 초기 월 목표 설정
        _uiState.update { it.copy(monthlyBudget = monthlyBudget) }
    }
    
    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun selectDate(date: Long) {
        _uiState.update { it.copy(selectedDate = date) }
    }
    
    fun changeMonth(year: Int, month: Int) {
        _selectedYearMonth.value = year to month
    }
    
    fun addNewTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            addTransaction(transaction)
                .onSuccess {
                    _uiEvent.emit(UiEvent("거래가 추가되었습니다"))
                }
                .onFailure { error ->
                    _uiEvent.emit(UiEvent("오류: ${error.message}"))
                }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun updateMonthlyBudget(amount: Int) {
        monthlyBudget = amount
        viewModelScope.launch {
            _uiEvent.emit(UiEvent("월 목표가 ${amount}원으로 변경되었습니다"))
        }
    }
    
    fun deleteTransaction(id: Long, isExpense: Boolean) {
        viewModelScope.launch {
            try {
                // 지출인 경우 이미지 파일도 삭제
                if (isExpense) {
                    val transaction = allTransactions.value
                        .filterIsInstance<Transaction.Expense>()
                        .find { it.id == id }
                    
                    transaction?.photoUri?.let { photoPath ->
                        // 파일 경로인 경우만 삭제 (content:// URI는 제외)
                        if (photoPath.startsWith("/")) {
                            com.example.accountbook.ui.utils.ImageUtils.deleteImageFile(photoPath)
                        }
                    }
                }
                
                repository.deleteTransaction(id, isExpense)
                _uiEvent.emit(UiEvent("삭제되었습니다"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent("삭제 실패: ${e.message}"))
            }
        }
    }
    
    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.addCategory(category)
                _uiEvent.emit(UiEvent("카테고리가 추가되었습니다"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent("카테고리 추가 실패: ${e.message}"))
            }
        }
    }
    
    fun getCurrentYearMonth(): Pair<Int, Int> {
        return _selectedYearMonth.value
    }
}

// UI 상태 데이터 클래스
data class MainUiState(
    val selectedTab: MainTab = MainTab.CALENDAR,
    val selectedDate: Long = System.currentTimeMillis(),
    val monthlyBudget: Int = 1_000_000,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class MainTab {
    CALENDAR, GALLERY, STATISTICS
}