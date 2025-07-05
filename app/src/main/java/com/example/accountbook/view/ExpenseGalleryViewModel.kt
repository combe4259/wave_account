package com.example.accountbook.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.accountbook.model.Expense
import com.example.accountbook.repository.ExpenseRepository

// 갤러리 화면의 상태를 관리
data class GalleryUiState(
    val expensesWithImages: List<Expense> = emptyList(),  // 사진이 있는 지출 내역들
    val isLoading: Boolean = true,                        // 로딩 상태
    val selectedExpense: Expense? = null,                 // 선택된 지출 (상세보기용)
    val showDetailDialog: Boolean = false                 // 상세보기 다이얼로그 표시 여부
)

class ExpenseGalleryViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(GalleryUiState(isLoading = true))
    val uiState: LiveData<GalleryUiState> = _uiState

    private val expensesLiveData = repository.getExpensesWithValidPhotos()

    // Observer를 별도 변수로 저장해서 나중에 정확히 제거할 수 있도록 합니다
    private val expensesObserver = Observer<List<Expense>> { expenses ->
        _uiState.value = _uiState.value?.copy(
            expensesWithImages = expenses ?: emptyList(),
            isLoading = false
        )
    }

    init {
        // 실제로 정의한 observer를 사용합니다
        expensesLiveData.observeForever(expensesObserver)
    }

    fun showImageDetail(expense: Expense) {
        _uiState.value = _uiState.value?.copy(
            selectedExpense = expense,
            showDetailDialog = true
        )
    }

    fun hideImageDetail() {
        _uiState.value = _uiState.value?.copy(
            selectedExpense = null,
            showDetailDialog = false
        )
    }

    fun refreshGallery() {
        _uiState.value = _uiState.value?.copy(isLoading = true)
    }

    override fun onCleared() {
        super.onCleared()
        expensesLiveData.removeObserver(expensesObserver)
    }
}