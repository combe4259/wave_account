package com.example.accountbook.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()
    
    // 사진이 있는 거래 목록 - Flow로 자동 업데이트
    val expensesWithPhotos: StateFlow<List<Transaction.Expense>> = 
        repository.getTransactionsWithPhotos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    fun selectExpense(expense: Transaction.Expense) {
        _uiState.update { 
            it.copy(
                selectedExpense = expense,
                showDetailDialog = true
            )
        }
    }
    
    fun dismissDialog() {
        _uiState.update { 
            it.copy(
                selectedExpense = null,
                showDetailDialog = false
            )
        }
    }
}

data class GalleryUiState(
    val selectedExpense: Transaction.Expense? = null,
    val showDetailDialog: Boolean = false
)