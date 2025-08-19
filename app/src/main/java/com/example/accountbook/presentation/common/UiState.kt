package com.example.accountbook.presentation.common

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class UiEvent(
    val message: String,
    val action: (() -> Unit)? = null
)