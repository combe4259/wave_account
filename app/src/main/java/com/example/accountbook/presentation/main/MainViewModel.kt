package com.example.accountbook.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountbook.domain.model.MonthlyStatistics
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.usecase.statistics.GetMonthlyStatisticsUseCase
import com.example.accountbook.domain.usecase.transaction.AddTransactionUseCase
import com.example.accountbook.domain.usecase.transaction.GetMonthlyTransactionsUseCase
import com.example.accountbook.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMonthlyTransactions: GetMonthlyTransactionsUseCase,
    private val getMonthlyStatistics: GetMonthlyStatisticsUseCase,
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