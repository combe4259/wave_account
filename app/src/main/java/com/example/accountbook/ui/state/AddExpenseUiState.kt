package com.example.accountbook.ui.state

import android.net.Uri


data class AddExpenseUiState(
    val productName: String = "",
    val amount: String = "",
    val selectedCategory: String = "식비",
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedImageUri: Uri? = null,
    val showDatePicker: Boolean = false,
    val isExpanded: Boolean = false,
    val showImageOptionsDialog: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val tempCameraUri: Uri? = null
) {
    val isValid: Boolean
        get() = productName.isNotBlank() && amount.isNotBlank()

    val amountAsDouble: Double
        get() = amount.toDoubleOrNull() ?: 0.0
}
